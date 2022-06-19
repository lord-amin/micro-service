package com.peykasa.authserver.validation;

import org.passay.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
public class PasswordRules {
    public List<String> validate(String obj, ValidationConfiguration.Validation validation) {
        List<org.passay.Rule> rules = new ArrayList<>();
        rules.add(new WhitespaceRule());
        int min = 1;
        int max = Integer.MAX_VALUE;

        if (validation.getMinLength() > 0) {
            min = validation.getMinLength();
        }
        if (validation.getMaxLength() > 0)
            max = validation.getMaxLength();
        if (min != 0 || max != 0) {
            rules.add(new LengthRule(min, max));
        }
        if (validation.isHasLower()) {
            rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        }
        if (validation.isHasUpper()) {
            rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        }
        if (validation.isHasSpecial()) {
            rules.add(new CharacterRule(EnglishCharacterData.Special, 1));
        }
        if (validation.isHasDigit()) {
            rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));
        }
        org.passay.PasswordValidator validator = new org.passay.PasswordValidator(rules);
        RuleResult result = validator.validate(new PasswordData(obj));
        List<String> messages = validator.getMessages(result);
        if (!result.isValid()) {
            return messages;
        }
        return null;
    }
}