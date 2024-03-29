package com.iyundao.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.iyundao.base.BaseController;
import com.iyundao.base.annotation.CurrentSubject;
import com.iyundao.base.utils.JsonResult;
import com.iyundao.base.utils.JsonUtils;
import com.iyundao.entity.Subject;
import com.iyundao.service.DepartService;
import com.iyundao.service.GroupService;
import com.iyundao.service.SubjectService;
import com.iyundao.service.UserRelationService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName: SubjectController
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/5/19 18:24
 * @Description: 控制层 - 机构
 * @Version: V2.0
 */
@RestController
@RequestMapping("/subject")
@RequiresRoles(value = {"user", "admin", "manager"}, logical = Logical.OR)
public class SubjectController extends BaseController {

    @Autowired
    private UserRelationService userRelationService;

    @Autowired
    private SubjectService subjectService;

    /**
     * @api {GET} /subject/list 机构列表
     * @apiGroup Subject
     * @apiVersion 2.0.0
     * @apiHeader {String} IYunDao-AssessToken token验证
     * @apiDescription 机构列表
     * @apiParamExample {json} 请求样例：
     *                /subject/list
     * @apiSuccess (200) {String} code 200:成功</br>
     *                                 404:请添加机构</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {
     * 	"code": 200,
     * 	"message": "成功",
     * 	"data": "['{'version':'1','id':'bd6886bc88e54ef0a36472efd95c744c','createdDate':'20190517111111','lastModifiedDate':'20190517111111','name':'总院','subjectType':'head'}','{'version':'1','id':'c72a2c6bd1e8428fac6706b217417831','createdDate':'20190517111111','lastModifiedDate':'20190517111111','name':'分院','subjectType':'head'}']"
     * }
     */
    @RequiresPermissions(PERMISSION_VIEW)
    @GetMapping("/list")
    public JsonResult list(@CurrentSubject Subject subject) {
        List<Subject> subjects = new ArrayList<>();
        if (subject == null) {
            subjects = subjectService.findAll();
        }else {
            subjects = subject.getSubjectType().equals(Subject.SUBJECT_TYPE.head)
                    ? subjectService.findAll()
                    : new ArrayList<Subject>(){};
        }
        if (CollectionUtils.isEmpty(subjects)) {
            subjects.add(subject);
        }
        if (CollectionUtils.isEmpty(subjects)) {
            return JsonResult.failure(404, "请添加机构");
        }
        JSONArray arr = new JSONArray();
        for (Subject s :subjects){
            s.setUserRelations(null);
            JSONObject json =getJson(s);
            json.put("code", s.getCode());
            json.put("name", s.getName());
            json.put("type", s.getSubjectType().getName());
            arr.add(json);
        }
        jsonResult.setData(arr);
        return jsonResult;
    }

    /**
     * @api {GET} /user_group/manager_list 列表
     * @apiGroup Subject
     * @apiHeader {String} IYunDao-AssessToken token验证
     * @apiVersion 2.0.0
     * @apiDescription 列表
     * @apiParamExample {json} 请求样例：
     *                /user_group/manager_list
     * @apiSuccess (200) {String} code 200:成功</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {{
     *     "code": 200,
     *     "message": "成功",
     *     "data": [
     *         {
     *             "name": "总院",
     *             "id": "bd6886bc88e54ef0a36472efd95c744c",
     *             "type": "总院"
     *         },
     *         {
     *             "name": "分院",
     *             "id": "c72a2c6bd1e8428fac6706b217417831",
     *             "type": "分院"
     *         },
     *         {
     *             "name": "修改机构",
     *             "id": "402881f46afdef14016afe28796c000b",
     *             "type": "其他"
     *         }
     *     ]
     * }
     */
    @RequiresRoles("admin")
    @GetMapping("/manager_list")
    public JsonResult managerList() {
        List<Subject> subjects = subjectService.findAll();
        JSONArray arr = new JSONArray();
        for (Subject subject : subjects) {
            JSONObject json =new JSONObject();
            json.put("id", subject.getId());
            json.put("name", subject.getName());
            switch (subject.getSubjectType().ordinal()) {
                case  0:
                    json.put("type", "总院");
                    break;
                case  1:
                    json.put("type", "分院");
                    break;
                case  2:
                    json.put("type", "其他");
                    break;
            }
            arr.add(json);
        }
        jsonResult.setData(arr);
        return jsonResult;
    }

    /**
     * @api {post} /subject/view 查看机构
     * @apiGroup Subject
     * @apiVersion 2.0.0
     * @apiDescription 查看机构
     * @apiHeader {String} IYunDao-AssessToken token验证
     * @apiParam {String} id
     * @apiParamExample {json} 请求样例：
     *                ?id=402881f46afdef14016afe28796c000b
     * @apiSuccess (200) {String} code 200:成功</br>
     *                                 404:未查询到此机构</br>
     *                                 600:参数异常</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": {
     *         "name": "修改机构",
     *         "id": "402881f46afdef14016afe28796c000b"
     *     }
     * }
     */
    @PostMapping("/view")
    public JsonResult view(String id) {
        if (StringUtils.isBlank(id)) {
            return JsonResult.paramError();
        }
        Subject subject = subjectService.find(id);
        if (subject == null) {
            return JsonResult.notFound("未查询到此机构");
        }
        try {
            JSONObject json = new JSONObject();
            json.put("id", subject.getId());
            json.put("name", subject.getName());
            jsonResult.setData(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResult;
    }

    /**
     * @api {POST} /subject/add 新增机构
     * @apiGroup Subject
     * @apiVersion 2.0.0
     * @apiDescription 新增机构
     * @apiHeader {String} IYunDao-AssessToken token验证
     * @apiParam {String} name
     * @apiParam {String} code
     * @apiParam {int} type
     * @apiParamExample {json} 请求样例：
     *                ?name=测试添加&type=1
     * @apiSuccess (200) {String} code 200:成功</br>
     *                                 404:已存在该机构</br>
     *                                 600:参数异常</br>
     *                                 601:机构名称/编号不能为空</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": {
     *         "createdDate": "20190618104404",
     *         "lastModifiedDate": "20190618104404",
     *         "name": "测试添加",
     *         "id": "402881916b68611a016b687853650002",
     *         "version": "0",
     *         "subjectType": "分院"
     *     }
     * }
     */
    @PostMapping("/add")
    public JsonResult add(String name, String code, @RequestParam(defaultValue = "0") int type) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(code)) {
            return JsonResult.failure(601, "机构名称/编号不能为空");
        }
        Subject subject = new Subject();
        subject.setName(name);
        subject.setCode(code);
        subject.setLastModifiedDate(new Date(System.currentTimeMillis()));
        subject.setCreatedDate(new Date(System.currentTimeMillis()));
        switch (type) {
            case 0 :
                subject.setSubjectType(Subject.SUBJECT_TYPE.head);
                break;
            case 1 :
                subject.setSubjectType(Subject.SUBJECT_TYPE.part);
                break;
            case 2 :
                subject.setSubjectType(Subject.SUBJECT_TYPE.etc);
                break;
            default :
                subject.setSubjectType(Subject.SUBJECT_TYPE.head);
                break;
        }
        subject = subjectService.save(subject);
        jsonResult.setData(converType(subject));
        return jsonResult;
    }

    /**
     * @api {POST} /subject/modify 修改机构
     * @apiGroup Subject
     * @apiVersion 2.0.0
     * @apiDescription 修改机构
     * @apiHeader {String} IYunDao-AssessToken token验证
     * @apiParam {String} id
     * @apiParam {String} name
     * @apiParam {String} code
     * @apiParam {int} type
     * @apiParamExample {json} 请求样例：
     *                ?id=402881f46afdef14016afe28796c000b&name=修改机构&type=2
     * @apiSuccess (200) {String} code 200:成功</br>
     *                                 404:未查询到此用户组</br>
     *                                 600:参数异常</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": {
     *         "createdDate": "20190528191706",
     *         "lastModifiedDate": "20190618104352",
     *         "name": "修改机构",
     *         "id": "402881f46afdef14016afe28796c000b",
     *         "version": "1",
     *         "subjectType": "其他"
     *     }
     * }
     */
    @PostMapping("/modify")
    public JsonResult modify(String id, String name, String code, @RequestParam(defaultValue = "3") int type) {
        if (StringUtils.isBlank(id)) {
            return JsonResult.paramError();
        }
        Subject subject = subjectService.find(id);
        if (subject == null) {
            return JsonResult.notFound("未查询到此机构");
        }
        subject.setLastModifiedDate(new Date(System.currentTimeMillis()));
        subject.setName(name);
        if (type != 3) {
            for (Subject.SUBJECT_TYPE subjectType : Subject.SUBJECT_TYPE.values()) {
                if (subjectType.ordinal() == type) {
                    subject.setSubjectType(subjectType);
                    break;
                }
            }
        }
        subject = subjectService.save(subject);
        jsonResult.setData(converType(subject));
        return jsonResult;
    }

    /**
     * @api {POST} /subject/checkCode 检测code
     * @apiGroup Subject
     * @apiVersion 2.0.0
     * @apiDescription 检测编号是否存在
     * @apiHeader {String} IYunDao-AssessToken token验证
     * @apiParam {String} code
     * @apiParamExample {json} 请求样例：
     *                ?code=1234
     * @apiSuccess (200) {String} code 200:成功</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": "可以使用"
     * }
     */
    @PostMapping("/checkCode")
    public JsonResult existCode(String code) {
        jsonResult.setData(subjectService.existsCode(code) ? "已存在" : "可以使用");
        return jsonResult;
    }


    /**
     * @api {post} /subject/distribution 分配
     * @apiGroup Subject
     * @apiVersion 2.0.0
     * @apiDescription 分配
     * @apiHeader {String} IYunDao-AssessToken token验证
     * @apiParam {String} id 必填,机构ID
     * @apiParam {String[]} departIds 部门ID集合
     * @apiParam {String[]} groupIds 组织ID集合
     * @apiParamExample {json} 请求样例：
     *                /subject/distribution
     * @apiSuccess (200) {String} code 200:成功</br>
     *                                 404:机构不存在</br>
     *                                 600:参数异常</br>
     *                                 601:机构ID不能为空</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {
     *  "code": 200,
     *  "message": "操作成功",
     *  "data": "{}"
     * }
     */
    @PostMapping("/distribution")
    public JsonResult addDepartsAndGroups(String id,
                                          String[] departIds,
                                          String[] groupIds) {
        if (StringUtils.isBlank(id)) {
            return JsonResult.failure(601, "机构ID不能为空");
        }
        Subject subject = subjectService.find(id);
        if (subject == null) {
            return JsonResult.notFound("机构不存在");
        }
        if (departIds == null && groupIds == null) {
            return JsonResult.paramError();
        }
        subject = subjectService.saveDepartAndGroup(subject, departIds, groupIds);
        return jsonResult;
    }

    /**
     * 转换type为相应json数据
     * @param subject
     * @return
     */
    private JSONObject converType(Subject subject) {
        try {
            JSONObject json = new JSONObject(getJson(subject));
            switch (subject.getSubjectType().ordinal()) {
                case 0 :
                    json.put("subjectType", "总院");
                    break;
                case 1 :
                    json.put("subjectType", "分院");
                    break;
                case 2 :
                    json.put("subjectType", "其他");
                    break;
                default :
                    json.put("subjectType", "总院");
                    break;
            }
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getJson(subject);
    }

}
