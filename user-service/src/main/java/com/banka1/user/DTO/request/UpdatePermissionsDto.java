package com.banka1.user.DTO.request;

import com.banka1.common.model.Permission;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePermissionsDto {

    private List<Permission> permissions;

}
