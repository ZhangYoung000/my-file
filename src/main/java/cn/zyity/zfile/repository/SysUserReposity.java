package cn.zyity.zfile.repository;

import cn.zyity.zfile.model.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserReposity extends JpaRepository<SysUser,Integer> {

    List<SysUser> findByUsername(String username);

    @Query("select r.role from SYS_ROLE r,SYS_USER u,SYS_USER_ROLE ur where ur.userId = u.id and ur.roleId = r.id and u.username = ?1")
    List<String> findRoleByUsername(String username);

    SysUser save(SysUser user);

    SysUser findFirstByDriverId(int driverId);



}
