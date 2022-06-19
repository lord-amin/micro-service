package com.peykasa.authserver.controller;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.audit.aspect.Auditable;
import com.peykasa.authserver.audit.aspect.provider.DeleteExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.DeleteProvider;
import com.peykasa.authserver.audit.aspect.provider.GetExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.Role.*;
import com.peykasa.authserver.audit.aspect.provider.SearchProvider;
import com.peykasa.authserver.exception.ExceptionTranslator;
import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.model.SimplePermission;
import com.peykasa.authserver.model.cto.BaseCTO;
import com.peykasa.authserver.model.cto.RoleCTO;
import com.peykasa.authserver.model.dto.RoleDTO;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.repository.BaseRepository;
import com.peykasa.authserver.repository.RoleRepository;
import com.peykasa.authserver.utility.ObjectMapper;
import com.peykasa.authserver.validation.ValidationContext;
//import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.peykasa.authserver.Constants.SPACE_NOT;
import static com.peykasa.authserver.utility.ObjectMapper.to;

/**
 * @author Yaser(amin) Sadeghi
 */
//@Api(tags = "Role controller", description = "Role controller", produces = "application/json")
@RequestMapping(value = Constants.ROLE_CONTEXT_PATH)
@RepositoryRestController
public class RoleController extends EntityController<RoleDTO, BaseCTO, Role, Long> {
    private final RoleRepository roleRepository;
    private final ValidationContext<RoleCTO> createRoleValidator;
    private final ValidationContext<RoleCTO> updateRoleValidator;
    @PersistenceContext
    private EntityManager em;

    @Autowired
    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        this.createRoleValidator = new ValidationContext<>();
        this.updateRoleValidator = new ValidationContext<>();
        this.createRoleValidator.addValidation(roleDTO -> StringUtils.isEmpty(roleDTO.getName()) ? Collections.singletonList(Constants.ROLE_EMPTY) : null);
        this.createRoleValidator.addValidation(userCTO -> (userCTO.getName()).contains(" ") ? Collections.singletonList(SPACE_NOT) : null);

        this.updateRoleValidator.addValidation(roleDTO -> "".equals(StringUtils.trim(roleDTO.getName())) ? Collections.singletonList(Constants.ROLE_BLANK) : null);
        this.updateRoleValidator.addValidation(roleCTO -> roleCTO.getName() != null && (roleCTO.getName()).contains(" ") ? Collections.singletonList(SPACE_NOT) : null);
    }

    @Auditable(provider = SearchProvider.class, context = Constants.ROLE, event = Constants.SEARCH)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PagedResources<Resource<RoleDTO>> findAll(String page, String size, String sort) {
        return super.findAll(page, size, sort);
    }

    @Auditable(provider = RoleGetProvider.class, exProvider = GetExceptionProvider.class, context = Constants.ROLE, event = Constants.SEARCH)
    @RequestMapping(path = Constants.ROLE_GET_PATH, method = RequestMethod.GET)
    @ResponseBody
    public RoleDTO findOne(@PathVariable("id") Long id) throws ResourceNotFoundException {
        return super.findOne(id);
    }

    @Override
    public String notFoundMessage() {
        return Constants.ROLE_NOT_FOUND;
    }


    @RequestMapping(path = Constants.ROLE_PERMISSIONS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Resources<SimplePermission> rolePermissions(@PathVariable("id") Long id) throws ResourceNotFoundException {
        Role one = roleRepository.findOne(id);
        if (one == null)
            throw new ResourceNotFoundException(null, String.format(notFoundMessage(), id));
        return new Resources<>(to(one.getPermissions(), SimplePermission.class));
    }

    @Auditable(provider = RoleCreateProvider.class, exProvider = RoleCreateExceptionProvider.class, context = Constants.ROLE, event = Constants.CREATE)
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public RoleDTO createRole(@RequestBody RoleCTO roleCTO) throws Exception {
        createRoleValidator.validate(roleCTO);
        Role created;
        try {
            created = roleRepository.save(to(roleCTO, Role.class));
        } catch (Exception e) {
            throw ExceptionTranslator.translateCreateRole(e, roleCTO);
        }
        em.detach(created);
        Long id = created.getId();
        created = roleRepository.findOne(created.getId());
        if (created == null) {
            throw new ResourceNotFoundException(null, String.format(notFoundMessage(), id));
        }
        return to(created, RoleDTO.class);
    }

    @Auditable(provider = RoleUpdateProvider.class, exProvider = RoleUpdateExceptionProvider.class, context = Constants.ROLE, event = Constants.UPDATE)
    @RequestMapping(path = Constants.ROLE_GET_PATH, method = RequestMethod.PATCH)
    @ResponseBody
    public RoleDTO updateRole(@RequestBody RoleCTO roleCTO, @PathVariable("id") Long id) throws Exception {
        if (id == null || id < 1)
            throw new ResourceNotFoundException(null, "Empty id " + id);
        Role found = roleRepository.findOne(id);
        if (found == null)
            throw new ResourceNotFoundException(null, "User with id " + id + " not found");
        updateRoleValidator.validate(roleCTO);
        RoleCTO preCTO = roleCTO.copy();
        Role preDatabaseState = found.copy();
        if (roleCTO.getName() != null)
            found.setName(roleCTO.getName());
        if (roleCTO.getDescription() != null)
            found.setDescription(roleCTO.getDescription());
        if (roleCTO.getPermissions() != null) {
            found.getPermissions().clear();
            if (roleCTO.getPermissions().size() > 0) {
                found.getPermissions().addAll(to(roleCTO.getPermissions(), Permission.class));
            }
        }
        if (roleCTO.getName() != null)
            roleCTO.setName(preDatabaseState.getName());
        if (roleCTO.getDescription() != null)
            roleCTO.setDescription(preDatabaseState.getDescription());
        if (roleCTO.getPermissions() != null)
            roleCTO.setPermissions(preDatabaseState.getPermissions().stream().map(permission -> new SimplePermission(permission.getId(), permission.getPermission())).collect(Collectors.toList()));
        Role role;
        try {
            role = roleRepository.saveAndFlush(found);
        } catch (Exception e) {
            ObjectMapper.copy(preCTO, roleCTO);
            throw ExceptionTranslator.translateUpdateROle(e, found.getPermissions(), found.getName());
        }

        return to(role, RoleDTO.class);
    }

    @Auditable(provider = DeleteProvider.class, exProvider = DeleteExceptionProvider.class, context = Constants.ROLE, event = Constants.DELETE)
    @RequestMapping(path = Constants.ROLE_GET_PATH, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteRole(@PathVariable("id") Long id) throws Exception {
        try {
            return super.delete(id);
        } catch (Exception e) {
            throw ExceptionTranslator.translateDeleteRole(e, notFoundMessage(), id);
        }
    }

    @Override
    public BaseRepository<Role, Long> getRepository() {
        return roleRepository;
    }

    @Override
    protected Class<RoleDTO> support() {
        return RoleDTO.class;
    }


}
