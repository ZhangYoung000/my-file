package cn.zyity.zfile.model.entity;

import lombok.Data;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;

@Proxy(lazy = false)
@Data
@Entity(name = "SYS_USER_ROLE")
public class SysUserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "rid")
    private int roleId;

    @Column(name = "uid")
    private int userId;

}
