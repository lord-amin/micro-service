//package com.peykasa.authserver.validation;
//
//import com.peykasa.authserver.exception.ValidationException;
//import com.peykasa.authserver.model.cto.RoleCTO;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.stereotype.Component;
//
///**
// * @author kamran
// */
//@Component
//public class RoleValidator {
//
//    public void validate(RoleCTO role) throws ValidationException {
//        String name = role.getName();
//        if (name != null) {
//            role.setName(name.trim());
//        }
//        if (role.getName().contains(" ")) {
//            throw new ValidationException("space character is not valid");
//        }
//        if (StringUtils.isEmpty(name)) {
//
//            throw new ValidationException(ROLE_EMPTY);
//        }
//    }
//}
