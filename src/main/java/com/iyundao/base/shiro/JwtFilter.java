package com.iyundao.base.shiro;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.iyundao.base.utils.JsonResult;
import com.iyundao.base.utils.JwtUtils;
import com.iyundao.entity.User;
import com.iyundao.entity.UserApp;
import com.iyundao.service.UserAppService;
import com.iyundao.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import sun.applet.resources.MsgAppletViewer;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ClassName: JwtFilter
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/7/16 16:08
 * @Description: JWT过滤
 * @Version: V2.0
 */
public class JwtFilter extends BasicHttpAuthenticationFilter {

    /**
     * LOGGER
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    @Autowired
    private RedisManager redisManager;
    @Value("${server.token.tokenExpireTime}")
    private Integer tokenExpireTime;
    @Value("${server.token.refreshTokenExpireTime}")
    private Integer refreshTokenExpireTime;
    @Value("${server.token.shiroCacheExpireTime}")
    private Integer shiroCacheExpireTime;
    @Value("${server.token.secretKey}")
    private String secretKey;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAppService userAppService;

    /**
     * 检测Header里ASSESSTOKEN字段
     * 判断是否登录
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader(SecurityConsts.IYUNDAO_ASSESS_TOKEN);
        return authorization != null;
    }

    /**
     * 是否登录路径
     */
    protected boolean isLoginUrl(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        return req.getRequestURI().startsWith(JwtUtils.LOGIN_URL);
    }

    /**
     * 是否APP登录
     * @param request
     * @return
     */
    private boolean isAppLogin(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        return req.getRequestURI().startsWith(JwtUtils.WX_START_URL);
    }

    /**
     * 登录验证
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String assessToken = httpServletRequest.getHeader(SecurityConsts.IYUNDAO_ASSESS_TOKEN);
        Subject subject = SecurityUtils.getSubject();
        String account = JwtUtils.getClaim(assessToken, SecurityConsts.ACCOUNT);
        User user = subject.getPrincipal() == null
                ? userService.findByAccount(account)
                : (User) subject.getPrincipal();
        JwtToken token = null;
        if (user != null) {
            token = new JwtToken(user.getAccount(), user.getPassword(), true, assessToken);
        } 
        boolean needLogin = !subject.isAuthenticated()
                || redisManager.hasKey(redisManager.getPREFIX_SHIRO_REFRESH_TOKEN() + account);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        try{
            if (needLogin) {
                subject.login(token);
                return true;
            }
            return false;
        } catch (UnknownAccountException ex) {
            return false;
        } catch (LockedAccountException ex) {
            return false;
        } catch (AccountException ex) {
            return false;
        } catch (TokenExpiredException ex) {
            return false;
        } catch (IncorrectCredentialsException ex){
            return false;
        }
    }
    
    private boolean executeAppLogin(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        String openId = req.getParameter("openId");
        if (StringUtils.isBlank(openId)) {
            return false;
        } 
        UserApp app = userAppService.findByOpenId(openId);
        if (app == null) {
            return false;
        }
        JwtToken token = null;
        User user = userService.findById(app.getUser().getId());
        String assessToken = SecurityConsts.PREFIX_SHIRO_REFRESH_TOKEN + user.getAccount();
        if (user != null) {
            token = new JwtToken(user.getAccount(), user.getPassword(), true, assessToken);
        }
        try {
            SecurityUtils.getSubject().login(token);
            return true;
        } catch (UnknownAccountException ex) {
            return false;
        } catch (LockedAccountException ex) {
            return false;
        } catch (AccountException ex) {
            return false;
        } catch (TokenExpiredException ex) {
            return false;
        } catch (IncorrectCredentialsException ex){
            return false;
        }
    }

    /**
     * 刷新AccessToken，进行判断RefreshToken是否过期，未过期就返回新的AccessToken且继续正常访问
     */
    private boolean refreshToken(ServletRequest request, ServletResponse response) {
        // 获取AccessToken(Shiro中getAuthzHeader方法已经实现)
        String token = this.getAuthzHeader(request);
        // 获取当前Token的帐号信息
        String account = JwtUtils.getClaim(token, SecurityConsts.ACCOUNT);
        String refreshTokenCacheKey = SecurityConsts.PREFIX_SHIRO_REFRESH_TOKEN + account;
        // 判断Redis中RefreshToken是否存在
        if (redisManager.hasKey(refreshTokenCacheKey)) {
            // 获取RefreshToken时间戳,及AccessToken中的时间戳
            // 相比如果一致，进行AccessToken刷新
            String currentTimeMillisRedis = redisManager.get(refreshTokenCacheKey).toString();
            String tokenMillis=JwtUtils.getClaim(token, SecurityConsts.CURRENT_TIME_MILLIS);

            if (tokenMillis.equals(currentTimeMillisRedis)) {

                // 设置RefreshToken中的时间戳为当前最新时间戳
                String currentTimeMillis = String.valueOf(System.currentTimeMillis());
                Integer refreshTokenExpireTime = JwtUtils.jwtUtils.refreshTokenExpireTime;
                redisManager.set(refreshTokenCacheKey, currentTimeMillis,refreshTokenExpireTime*60L);

                // 刷新AccessToken，为当前最新时间戳
                token = JwtUtils.sign(account, currentTimeMillis);

                // 使用AccessToken 再次提交给ShiroRealm进行认证，如果没有抛出异常则登入成功，返回true
                JwtToken jwtToken = new JwtToken(token);
                this.getSubject(request, response).login(jwtToken);

                // 设置响应的Header头新Token
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.setHeader(SecurityConsts.IYUNDAO_ASSESS_TOKEN, token);
                httpServletResponse.setHeader("Access-Control-Expose-Headers", SecurityConsts.IYUNDAO_ASSESS_TOKEN);
                return true;
            }
        }
        return false;
    }

    /**
     * 是否允许访问
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginUrl(request)) {
            return true;
        }
        if (isAppLogin(request)) {
            return executeAppLogin(request);
        } else if (isLoginAttempt(request, response)) {
            try {
                this.executeLogin(request, response);
            } catch (Exception e) {
                String msg = e.getMessage();
                Throwable throwable = e.getCause();
                if (throwable != null && throwable instanceof SignatureVerificationException) {
                    msg = "Token或者密钥不正确(" + throwable.getMessage() + ")";
                } else if (throwable != null && throwable instanceof TokenExpiredException) {
                    // AccessToken已过期
                    if (this.refreshToken(request, response)) {
                        return true;
                    } else {
                        msg = "Token已过期(" + throwable.getMessage() + ")";
                    }
                } else {
                    if (throwable != null) {
                        msg = throwable.getMessage();
                    }
                }
                this.response401(request, response, msg);
                return false;
            }
        }else {
            this.response401(request, response, "缺少IYunDao-AssessToken,禁止访问");
            return false;
        }
        return true;
    }

    /**
     * 401非法请求
     * @param req
     * @param resp
     */
    private JsonResult response401(ServletRequest req, ServletResponse resp, String msg) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setCharacterEncoding("UTF-8");

        JsonResult result = JsonResult.success();
        result.setCode(400);
        result.setMessage(msg);
        try {
            PrintWriter pw = resp.getWriter();
            JSONObject json = new JSONObject();
            json.put("code", result.getCode());
            json.put("message", result.getMessage());
            json.put("data", result.getData());
            pw.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
