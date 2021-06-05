package cn.zyity.zfile.service;

import cn.zyity.zfile.model.entity.SysUser;
import cn.zyity.zfile.repository.SysUserReposity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class SystemUserService {
    @Resource
    SysUserReposity userReposity;

    public SysUser findUserByUsername(String username) {
        List<SysUser> users = userReposity.findByUsername(username);
        if (users == null || users.size() == 0) {
            return null;
        } else {
            SysUser user = users.get(0);
            return user;
        }
    }

    public String findUserRoleByUsername(String username) {
        List<String> roleNames = userReposity.findRoleByUsername(username);
        if (roleNames == null || roleNames.size() == 0) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String role :
                    roleNames) {
                sb.append(role);
                sb.append(",");
            }
            String roles = sb.toString();
            return roles.substring(0, roles.length() - 1);
        }
    }
}
