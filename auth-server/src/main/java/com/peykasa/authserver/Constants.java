package com.peykasa.authserver;

/**
 * @author Yaser(amin) Sadeghi
 */
public class Constants {
    public static final String _DELETED_AT= "_DELETED_at_";
    public static final String AUTHORITY_SUPER_ADMIN = "auth_server_super_admin";
    public static final String ROLE = "Role";
    public static final String USER = "User";
    public static final String CLIENT= "Client";
    public static final String AUTH = "Authentication";
    public static final String CREATE = "Create";
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String SEARCH = "Search";
    public static final String CHANGE_PASSWORD = "ChangePassword";
    public static final String LOGIN = "Login";
    public static final String LOGOUT = "Logout";
    public static final String SUCCESS = "Success";
    public static final String FAIL = "Fail";
    public static final String USER_CONTEXT_PATH = "/api/users";
    public static final String CLIENT_CONTEXT_PATH = "/api/clients";
    public static final String USER_GET = "/{id}";
    public static final String USER_ROLES_PATH = "/{id}/roles";
    public static final String USER_CHANGE_PASSWORD_PATH = "/change-password";
    public static final String USER_DELETED_PATH = "/deleted";
    public static final String USER_LOGOUT_PATH = "/logout";
    public static final String USER_NOT_FOUND = "User id %s not found";
    public static final String ROLE_CONTEXT_PATH = "/api/roles";
    public static final String ROLE_PERMISSIONS_PATH = "/{id}/permissions";
    public static final String ROLE_GET_PATH = "/{id}";
    public static final String ROLE_NOT_FOUND = "Role id %s not found";
    public static final String NOTE_FOUND_ERROR = "note_found_error";
    public static final String DUPLICATE_ERROR = "duplicate_error";
    public static final String CLIENT_ERROR = "client_error";
    public static final String RELATION_ERROR = "relation_error";
    public static final String VALIDATION_ERROR = "validation_error";
    public static final String UNHANDLED_ERROR = "unhandled_error";
    public static final String PERMISSION_DENIED_ERROR = "permission_denied_error";

    public static final String VALIDATION_NON_PRINTABLE = "{0} has non ascii character ";
    public static final String FIRST_NAME_NULL = "firstName is null or empty";
    public static final String LAST_NAME_NULL = "lastName is null or empty";
    public static final String USER_NAME_NULL = "username is null or empty";
    public static final String IS_NULL = "{0} is null or empty";
    public static final String IS_BLANK= "{0} is empty";
    public static final String MAX_LENGTH= "{0} max length is {1}";
    public static final String USER_NAME_EMPTY = "username is empty";
    public static final String SPACE_NOT = "space character is not valid";
    public static final String PASSWORD_NULL = "password is null or empty";
    public final static String PASSWORD_MSG = "The password and confirm password is not equal";
    public static final String OLD_PASSWORD_NULL = "Old password is null";
    public static final String NEW_PASSWORD_NULL = "New password is null";
    public static final String CONFIRM_PASSWORD_NULL = "Confirm password is null";
    public static final String CONFIRM_PASSWORD_FAIL = "Confirm and pass not equals";
    public static final String LAST_NAME_EMPTY = "lastName is empty";
    public static final String FIRST_NAME_EMPTY = "firstName is empty";
    public static final String OLD_PASS_FAIL = "The old password is not correct ";
    public static final String OLD_AND_NEW_PASS_FAIL = "The old password and new password is same";
    public static final String CAN_NOT_DELETE_YOURSELF = "Can not delete yourself";
    public static final String ROLE_EMPTY = "name is null or empty";
    public static final String ROLE_BLANK = "name is empty";
    public static final String INVALID_USER = "Invalid_User";
    public static final String ACCESS_DENIED_ADMIN = "Access denied,Could not delete an admin user";
    public static final String AUTHORIZE_URL = "/authorize";
    public static final String AUTHORIZES_URL = "/authorizes";

    private Constants() {
    }
}
