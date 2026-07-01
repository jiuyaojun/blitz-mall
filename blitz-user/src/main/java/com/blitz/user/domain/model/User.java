package com.blitz.user.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @JsonIgnore
    private String password;  // BCrypt hashed
    private String phone;
    private String email;
    private String avatar;
    private Long roleId;
    private Integer status;   // 1-normal 0-disabled
    private LocalDateTime lastLogin;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    // Behavior methods
    public boolean isDisabled() { return status != null && status == 0; }
    public void disable() { this.status = 0; }
    public void enable() { this.status = 1; }
    public void recordLogin() { this.lastLogin = LocalDateTime.now(); }
}
