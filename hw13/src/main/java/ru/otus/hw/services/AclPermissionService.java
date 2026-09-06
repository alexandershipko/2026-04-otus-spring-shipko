package ru.otus.hw.services;

import org.springframework.security.acls.model.Permission;

import java.io.Serializable;

public interface AclPermissionService {

    void grantOwnerPermissions(Object domainObject, Permission... permissions);

    void deleteAcl(Class<?> domainType, Serializable id);

}
