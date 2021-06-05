package cn.zyity.zfile.security;

import cn.zyity.zfile.model.dto.SystemConfigDTO;
import cn.zyity.zfile.model.entity.SysUser;
import cn.zyity.zfile.service.SystemConfigService;
import cn.zyity.zfile.service.SystemUserService;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;

/**
 */
public class MyUserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SystemConfigService systemConfigService;
    @Resource
    SystemUserService userService;

    /**
     * 授权的时候是对角色授权，认证的时候应该基于资源，而不是角色，因为资源是不变的，而用户的角色是会变的
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        if (!Objects.equals(systemConfig.getUsername(), username)) {
            SysUser user = userService.findUserByUsername(username);
            System.out.println("======user========");
            System.out.println(user);

            if (user == null) {
                throw new UsernameNotFoundException("用户名不存在");
            } else {
                System.out.println("======roles========");
                System.out.println(userService.findUserRoleByUsername(username));
                return new User(user.getUsername(), user.getPsd(), AuthorityUtils.createAuthorityList(String.valueOf(user.getDriverId())));
            }
        } else {
            return new User(systemConfig.getUsername(), systemConfig.getPassword(), AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        }
    }

}