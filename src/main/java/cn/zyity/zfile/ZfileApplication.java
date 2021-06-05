package cn.zyity.zfile;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.MD5;
import cn.zyity.zfile.security.Md5PasswordEncoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 */
@EnableAsync
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class ZfileApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ZfileApplication.class, args);
       /* String s = SecureUtil.md5("123456");
        System.out.println(s);*/

    }
    @Override //为了打包springboot项目
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder builder) {
        return builder.sources(this.getClass());
    }

}
