package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@RequiredArgsConstructor
@Service
public class AclPermissionServiceImpl implements AclPermissionService {

    private final MutableAclService mutableAclService;

    @Override
    public void grantOwnerPermissions(Object domainObject, Permission... permissions) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var owner = new PrincipalSid(authentication);
        var objectIdentity = new ObjectIdentityImpl(domainObject);

        var acl = getOrCreateAcl(objectIdentity);
        for (var permission : permissions) {
            acl.insertAce(acl.getEntries().size(), permission, owner, true);
        }

        mutableAclService.updateAcl(acl);
    }

    @Override
    public void deleteAcl(Class<?> domainType, Serializable id) {
        mutableAclService.deleteAcl(new ObjectIdentityImpl(domainType, id), true);
    }

    private MutableAcl getOrCreateAcl(ObjectIdentity objectIdentity) {
        try {
            return (MutableAcl) mutableAclService.readAclById(objectIdentity);
        } catch (NotFoundException e) {
            return mutableAclService.createAcl(objectIdentity);
        }
    }

}
