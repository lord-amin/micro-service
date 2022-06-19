//package com.peykasa.authserver.validation;
//
//import com.peykasa.authserver.exception.ValidationException;
//import com.peykasa.authserver.model.UserPassword;
//import com.peykasa.authserver.model.entity.User;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
///**
// * @author Yaser(amin) Sadeghi
// */
//@Component
//public class UserPasswordValidator extends PasswordValidator {
//
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    public UserPasswordValidator(PasswordEncoder passwordEncoder) {
//        this.passwordEncoder = passwordEncoder;
//    }
//
//
//    public void validate(UserPassword data) throws ValidationException {
//        if (StringUtils.isEmpty(data.getOldPassword())) {
//            throw new ValidationException("old password is null or empty");
//        }
//        if (StringUtils.isEmpty(data.getNewPassword())) {
//            throw new ValidationException("new password is null or empty");
//        }
//        if (StringUtils.isEmpty(data.getConfirmPassword())) {
//            throw new ValidationException("confirm password is null or empty");
//        }
//        if (!data.getConfirmPassword().equals(data.getNewPassword())) {
//            throw new ValidationException("new password is not equals to confirm");
//        }
//        if (data.getOldPassword().equals(data.getNewPassword())) {
//            throw new ValidationException("The old password and new password is equal");
//        }
//        User obj1 = new User();
//        obj1.setPassword(data.getNewPassword());
//        super.validate(data.getNewPassword());
//        data.setNewPassword(passwordEncoder.encode(data.getNewPassword()));
//
//    }
//}