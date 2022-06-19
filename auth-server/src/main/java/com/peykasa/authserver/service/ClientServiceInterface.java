package com.peykasa.authserver.service;

import com.peykasa.authserver.exception.DuplicateResourceException;
import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.exception.ValidationException;
import com.peykasa.authserver.model.cto.CreateClientCTO;
import com.peykasa.authserver.model.cto.UpdateClientCTO;
import com.peykasa.authserver.model.dto.UserClientDTO;

/**
 * @author Taher Khorshidi
 */

public interface ClientServiceInterface {

    UserClientDTO createUserClient(CreateClientCTO userClientCTO) throws DuplicateResourceException;

    UserClientDTO updateUserClient(UpdateClientCTO userClientCTO) throws ResourceNotFoundException, ValidationException;


}
