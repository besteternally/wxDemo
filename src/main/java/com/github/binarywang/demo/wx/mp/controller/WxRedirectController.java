package com.github.binarywang.demo.wx.mp.controller;

import com.github.binarywang.demo.wx.mp.config.WxMpConfiguration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.xml.ws.Action;
import java.net.URLEncoder;

/**
 * @author Edward
 */
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/wx/redirect/{appid}")
public class WxRedirectController {
    private final WxMpService wxService;

    @RequestMapping("/greet")
    public String greetUser(@PathVariable String appid, @RequestParam String code, ModelMap map) {
        if (!this.wxService.switchover(appid)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appid));
        }
        try {
            WxMpOAuth2AccessToken accessToken = wxService.oauth2getAccessToken(code);
            WxMpUser user = wxService.oauth2getUserInfo(accessToken, null);
            map.put("user", user);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }

        return "greet_user";
    }

    @GetMapping("/authorize")
    public String authoirze(){
        String url="http://hyj.free.idcfengye.com/wx/redirect/wx625b5bf6359bda14/userInfo";
        String authUrl=wxService.oauth2buildAuthorizationUrl(url, WxConsts.OAuth2Scope.SNSAPI_USERINFO, null);
        log.info("用户点击了授权的链接，重定向到页面：{}",authUrl);
        log.info("打印系统中的access_token{}",wxService.getWxMpConfigStorage().getAccessToken());
        //ModelAndView modelAndView=new ModelAndView(authUrl);
        return "redirect:"+authUrl;
    }

    @GetMapping("/userInfo")
    public String userInfo(@RequestParam String code,ModelMap map){
        try {
            WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxService.oauth2getAccessToken(code);
            WxMpUser wxMpUser = wxService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);
            map.put("user",wxMpUser);
            log.info("得到了user：{}",wxMpUser.toString());
        } catch (WxErrorException e) {
            log.info("获取access_Token失败，错误原因：{}",e);
        }
        return "greet_user";
    }

    @GetMapping("/location")
    public String location(ModelMap map){
        try {
            String jsapiTicket = wxService.getJsapiTicket();
            log.info("获取到了ticket：{}",jsapiTicket);
            String url="http://hyj.free.idcfengye.com/wx/redirect/wx625b5bf6359bda14/location";
            WxJsapiSignature jsapiSignature = wxService.createJsapiSignature(url);
            map.put("jsapiSignature",jsapiSignature);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        return "jsSDK";
    }

}

