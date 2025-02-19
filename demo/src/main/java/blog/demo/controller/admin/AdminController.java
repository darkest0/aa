package blog.demo.controller.admin;

import blog.demo.Input.LoginInput;
import blog.demo.entity.TbAdminUser;
import blog.demo.service.ITbAdminUserService;
import blog.demo.service.ITbBlogCategoryService;
import blog.demo.service.ITbBlogCommentService;
import blog.demo.service.ITbBlogService;
import blog.demo.service.ITbBlogTagService;
import blog.demo.service.ITbLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author towa
 * 登陆 及 admin 首页
 */
@RequestMapping("/admin")
@Controller
public class AdminController {

    @Autowired
    ITbAdminUserService adminUserService;
    @Autowired
    ITbBlogCategoryService categoryService;
    @Autowired
    ITbBlogCommentService  commentService;
    @Autowired
    ITbBlogTagService tagService;
    @Autowired
    ITbBlogService blogService;
    @Autowired
    ITbLinkService linkService;

    @GetMapping({"/login"})
    public String login(){
        return "admin/login";
    }

    @GetMapping({"/test"})
    public String test(){ return "admin/test"; }

    @PostMapping("/login")
    public String login( LoginInput input, HttpSession session){
        if (StringUtils.isEmpty(input.getVerifyCode())){
            session.setAttribute("errorMsg","验证码不能为空");
            return "admin/login";
        }
        if (StringUtils.isEmpty(input.getUserName())||StringUtils.isEmpty(input.getPassword())){
            session.setAttribute("errorMsg","用户名或密码不能为空");
            return "admin/login";
        }
        String kaptchaCode = session.getAttribute("verifyCode")+"";
        if (StringUtils.isEmpty(kaptchaCode)||!input.getVerifyCode().equals(kaptchaCode)){
            session.setAttribute("errorMsg","验证码错误");
            return "admin/login";
        }
        TbAdminUser adminUser = adminUserService.login(input.getUserName(),input.getPassword());
        if (adminUser!=null){
            session.setAttribute("loginUser",adminUser.getNickName());
            session.setAttribute("loginUserId",adminUser.getAdminUserId());
            return "redirect:/admin/index";
        }else {
            session.setAttribute("errorMsg","登陆失败！");
            return "admin/login";
        }
    }

    @GetMapping({"", "/", "/index", "/index.html"})
    public String index(HttpServletRequest request){
        request.setAttribute("path","index");
        request.setAttribute("categoryCount",categoryService.getTotalCategory());
        request.setAttribute("blogCount",blogService.getTotalBlog());
        request.setAttribute("linkCount",linkService.getTotalLink());
        request.setAttribute("tagCount",tagService.getTotalTag());
        request.setAttribute("commentCount",commentService.getTotalComment());
        request.setAttribute("path","index");
        return "admin/index";
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request){
        Integer loginUserId = (Integer) request.getSession().getAttribute("loginUserId");
        TbAdminUser adminUser = adminUserService.getById(loginUserId);
        if (adminUser ==null){
            return "admin/login";
        }
        request.setAttribute("path","profile");
        request.setAttribute("loginUserName",adminUser.getLoginUserName());
        request.setAttribute("nickName",adminUser.getNickName());
        return "admin/profile";
    }

    @PostMapping("/profile/password")
    @ResponseBody
    public String passwordUpdate(HttpServletRequest request,@RequestParam("originalPassword") String originalPassword,
                                 @RequestParam("newPassword") String newPassword){

        if (StringUtils.isEmpty(originalPassword)||StringUtils.isEmpty(newPassword)){
            return "参数不能为空";
        }
        Integer loginUserid =(Integer) request.getSession().getAttribute("loginUserId");
        if (adminUserService.updatePassword(loginUserid,originalPassword,newPassword)){
            //清空session中的数据，前端控制转跳登陆界面
            request.getSession().removeAttribute("loginUserId");
            request.getSession().removeAttribute("loginUser");
            request.getSession().removeAttribute("errorMsg");
            return "success";
        }else {
            return "修改失败";
        }
    }
    @PostMapping("/profile/name")
    @ResponseBody
    public String nameUpdate(HttpServletRequest request, @RequestParam("loginUserName") String loginUserName,
                             @RequestParam("nickName") String nickName){

        if (StringUtils.isEmpty(loginUserName)||StringUtils.isEmpty(nickName)){
            return "参数不能为空";
        }
        Integer loginUserId = (Integer) request.getSession().getAttribute("loginUserId");
        if (adminUserService.updateName(loginUserId,loginUserName,nickName)){
            return "success";
        }else {
            return "修改失败";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute("loginUserId");
        request.getSession().removeAttribute("loginUser");
        request.getSession().removeAttribute("errorMsg");
        return "admin/login";
    }
}
