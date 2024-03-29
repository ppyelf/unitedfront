package com.iyundao.base.resolver;

import com.iyundao.base.annotation.CurrentUser;
import com.iyundao.base.shiro.SecurityConsts;
import com.iyundao.base.utils.JwtUtils;
import com.iyundao.entity.User;
import com.iyundao.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName: CurrentUserResolver
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/7/13 16:48
 * @Description: 当前用户参数解析器
 * @Version: V2.0
 */
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Subject subject = SecurityUtils.getSubject();
        return parameter.getParameterType().isAssignableFrom(User.class)
                && parameter.hasParameterAnnotation(CurrentUser.class)
                && subject.isAuthenticated();
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest req, WebDataBinderFactory webDataBinderFactory) throws Exception {
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        user = user == null ? (User) SecurityUtils.getSubject().getSession().getAttribute("currentUser") : user;
        if (user == null) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String token = request.getHeader("IYunDao-AssessToken");
            user = userService.findByAccount(JwtUtils.getClaim(token, SecurityConsts.ACCOUNT));
        } 
        SecurityUtils.getSubject().getSession().setAttribute("currentUser", user);
        return user == null ? null : user;
    }
}
