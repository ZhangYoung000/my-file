package cn.zyity.zfile.repository;

import cn.zyity.zfile.model.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<SysUserRole, Integer> {

}
