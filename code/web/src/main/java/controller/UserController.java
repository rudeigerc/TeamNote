package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.mongodb.User;
import model.mysql.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import service.UserBasicService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

/**
 * Created by qjr on 2017/7/7.
 */
@Controller
public class UserController {
    private final UserBasicService userBasicService;
    @Autowired

    public UserController(UserBasicService userBasicService) {
        this.userBasicService = userBasicService;
    }

    @RequestMapping("/userdetail")
    @ResponseBody
    public String getUserDetail() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        UserInfo userInfo = userBasicService.getUserInfoByUsername(username);
        int userId = userInfo.getUserId();
        User user = userBasicService.getUserById(userId);
        Gson gson = new Gson();
        String userInfoString = gson.toJson(userInfo);
        String userString = gson.toJson(user);

        JsonObject json = new JsonObject();
        json.addProperty("userInfo", userInfoString);
        json.addProperty("user", userString);

        return json.toString();
    }

    @RequestMapping("/updateUserProfile")
    @ResponseBody
    public String updateUserProfile(@RequestParam("email") String email, @RequestParam("phone") String phone,
                                    @RequestParam("ps") String ps) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        UserInfo userInfo = userBasicService.getUserInfoByUsername(username);
        int userId = userInfo.getUserId();
        userBasicService.updateUserProfile(userId, email, phone, ps);

        JsonObject json = new JsonObject();
        json.addProperty("result", "success");
        return json.toString();
    }

    @RequestMapping("/updateUserPassword")
    @ResponseBody
    public String updateUserPassword(@RequestParam("originalRawPassword") String originalRawPassword,
                                     @RequestParam("newRawPassword") String newRawPassword) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        UserInfo userInfo = userBasicService.getUserInfoByUsername(username);
        int userId = userInfo.getUserId();

        JsonObject json = new JsonObject();
        if (userBasicService.updatePassword(userId, originalRawPassword, newRawPassword) == 1) {
            json.addProperty("result", "success");
        } else {
            json.addProperty("result", "origin password wrong");
        }
        return json.toString();
    }

    @RequestMapping("/uploadAvatar")
    public String uploadavatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        ServletContext servletContext = request.getServletContext();
        String avatarName = Long.toString(System.currentTimeMillis()) + file.getOriginalFilename();
        String destPath = servletContext.getRealPath("/image/avatar/") + avatarName;
        file.transferTo(new File(destPath));

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        UserInfo userInfo = userBasicService.getUserInfoByUsername(username);
        int userId = userInfo.getUserId();

        userBasicService.updateavatar(userId, "image/avatar/" + avatarName, servletContext.getRealPath("/"));
        return "redirect:/settings.jsp";
    }
}