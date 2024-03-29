package com.iyundao.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iyundao.base.BaseController;
import com.iyundao.base.annotation.CurrentUser;
import com.iyundao.base.shiro.JwtToken;
import com.iyundao.base.shiro.SecurityConsts;
import com.iyundao.base.utils.JsonResult;
import com.iyundao.entity.User;
import com.iyundao.entity.UserRelation;
import com.iyundao.service.SubjectService;
import com.iyundao.service.UserRelationService;
import com.iyundao.service.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName: IndexController
 * @project: iyundao
 * @author: 念
 * @Date: 2019/5/13 16:40
 * @Description: 首页
 * @Version: V1.0
 */
@RestController
@RequestMapping("/")
public class IndexController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private UserRelationService userRelationService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectService subjectService;

    @CurrentUser
    private User user;

    /**
     * @api {POST} /login 用户登录
     * @apiGroup index
     * @apiVersion 2.0.0
     * @apiDescription 用于用户登录
     * @apiParam {String} account 用户名
     * @apiParam {String} password 密码
     * @apriParam {boolean} rememberMe 记住我
     * @apiParamExample {json} 请求样例：
     *                ?account=admin&password=admin
     * @apiSuccess (200) {String} code 200:成功</br>
     *                                 201:用户名密码错误</br>
     *                                 400:账号异常</br>
     *                                 404:账号不存在</br>
     *                                 600:参数错误</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {
     * 	"code": 200,
     * 	"message": "登录成功",
     * 	"data": "{'version':'0','id':'0a4179fc06cb49e3ac0db7bcc8cf0882','createdDate':'20190517111111','lastModifiedDate':'20190517111111','name':'管理员','password':'b356a1a11a067620275401a5a3de04300bf0c47267071e06','status':'normal','remark':'未填写','sex':'0','salt':'3a10624a300f4670','account':'admin','userType':'amdin'}"
     * }
     */
    @PostMapping("/login")
    public JsonResult login(String account, String password, boolean rememberMe, Model model, HttpServletRequest req, HttpServletResponse resp) {
        Subject subject = SecurityUtils.getSubject();
        JwtToken token = new JwtToken(account, password, rememberMe, SecurityConsts.PREFIX_SHIRO_REFRESH_TOKEN + account);
        if (subject.isAuthenticated()) {
            return loginSuccess(account, resp);
        }
        //根据权限，指定返回数据
        jsonResult = login(subject, token);
        if (jsonResult.getCode() == JsonResult.CODE_SUCCESS) {
            User user = (User) subject.getPrincipal();
            if (user != null && user.getAccount().equals(account)) {
                return loginSuccess(account, resp);
            }
        } 
        return JsonResult.failure(400,"账号不存在");
    }


    /**
     * @api {GET} /subjectList 个人机构列表
     * @apiGroup index
     * @apiVersion 2.0.0
     * @apiDescription 个人机构列表
     * @apiParam {String} id 用户ID
     * @apiParamExample {json} 请求样例：
     *                /subjectList
     * @apiSuccess (200) {String} code 200:成功</br>
     *                                 404:机构不存在/ID为空</br>
     *                                 100:未加入任何机构</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {
     * 	"code": 200,
     * 	"message": "登录成功",
     * 	"data": "{'version':'0','id':'0a4179fc06cb49e3ac0db7bcc8cf0882','createdDate':'20190517111111','lastModifiedDate':'20190517111111','name':'管理员','password':'b356a1a11a067620275401a5a3de04300bf0c47267071e06','status':'normal','remark':'未填写','sex':'0','salt':'3a10624a300f4670','account':'admin','userType':'amdin'}"
     * }
     */
    @GetMapping("/subjectList")
    public JsonResult subjectList(@CurrentUser User user) {
        Set<com.iyundao.entity.Subject> set = new HashSet<>();
        List<UserRelation> list = userRelationService.findByUserId(user.getId());
        for (UserRelation ur : list) {
            set.add(ur.getSubject());
        }
        JSONArray arr = new JSONArray();
        for (com.iyundao.entity.Subject subject : set) {
            JSONObject json = new JSONObject();
            json.put("id", subject.getId());
            json.put("name", subject.getName());
            arr.add(json);
        }
        jsonResult = JsonResult.success();
        if (CollectionUtils.isEmpty(arr)) {
            jsonResult.setCode(100);
            jsonResult.setMessage("未加入任何和机构");
            return jsonResult;
        } 
        jsonResult.setData(arr);
        return jsonResult;
    }

    /**
     * @api {POST} /changeSubject 切换医院机构
     * @apiGroup 首页
     * @apiVersion 2.0.0
     * @apiDescription 切换医院机构
     * @apiParam {String} id 机构ID
     * @apiParamExample {json} 请求样例：
     *                ?id=bfc5bd62010f467cbbe98c9e4741733b
     * @apiSuccess (200) {String} code 200:成功</br>
     *                                 404:机构不存在/ID为空</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccess (200) {String} data 返回用户信息
     * @apiSuccessExample {json} 返回样例:
     * {
     * 	"code": 200,
     * 	"message": "登录成功",
     * 	"data": "{'version':'0','id':'0a4179fc06cb49e3ac0db7bcc8cf0882','createdDate':'20190517111111','lastModifiedDate':'20190517111111','name':'管理员','password':'b356a1a11a067620275401a5a3de04300bf0c47267071e06','status':'normal','remark':'未填写','sex':'0','salt':'3a10624a300f4670','account':'admin','userType':'amdin'}"
     * }
     */
    @PostMapping("/changeSubject")
    public JsonResult changeSubject(String id) {
        com.iyundao.entity.Subject currentSubject = subjectService.find(id);
        if (currentSubject == null) {
            return JsonResult.notFound("机构不存在/ID为空");
        }
        Session session = SecurityUtils.getSubject().getSession();
        JSONObject json = new JSONObject();
        session.setAttribute("currentSubject", json);
        jsonResult.setData(json);
        return jsonResult;
    }


    @RequestMapping("/unauthorized")
    public JsonResult unauthorized() {
        jsonResult.setCode(401);
        jsonResult.setMessage("无权限/未登录");
        return jsonResult;
    }
    /**
     * 解除admin 用户的限制登录
     * 写死的 方便测试
     * @return
     */
    @RequestMapping("/unlockAccount")
    public String unlockAccount(Model model){
        model.addAttribute("msg","用户解锁成功");
        return "login";
    }

    /**
     * @api {POST} /logout 退出登录
     * @apiGroup Index
     * @apiVersion 2.0.0
     * @apiDescription 退出登录
     * @apiParamExample {json} 请求样例：
     * /out
     * @apiSuccess (200) {String} code 200:成功</br>
     * @apiSuccess (200) {String} message 信息
     * @apiSuccessExample {json} 返回样例:
     * {
     * 	"code": 200,
     * 	"message": "退出登录成功"
     * }
     */
    @PostMapping("/logout")
    public JsonResult out(HttpServletRequest req) {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        req.getSession().removeAttribute("currentUser");
        req.getSession().removeAttribute("currentSubject");
        return JsonResult.success("退出登录成功");
    }
}
