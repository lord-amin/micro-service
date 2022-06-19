package com.peykasa.authserver.service;

import com.peykasa.authserver.controller.UserClientMapper;
import com.peykasa.authserver.exception.DuplicateResourceException;
import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.exception.ValidationException;
import com.peykasa.authserver.model.UserPassword;
import com.peykasa.authserver.model.cto.CreateClientCTO;
import com.peykasa.authserver.model.cto.UpdateClientCTO;
import com.peykasa.authserver.model.dto.UserClientDTO;
import com.peykasa.authserver.model.entity.Client;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.ClientRepository;
import com.peykasa.authserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Taher Khorshidi
 */
@Service
@RequiredArgsConstructor
public class ClientService implements ClientDetailsService, ClientServiceInterface {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final UserClientMapper userClientMapper;
    private final UserService userService;
    private final HttpServletRequest request;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Object loaded_client = request.getAttribute("loaded_client");
        if (loaded_client != null)
            return (ClientDetails) loaded_client;
        Client byClientId = clientRepository.findByClientId(clientId);
        request.setAttribute("loaded_client", byClientId);
        return byClientId;
    }

    @Transactional()
    @Override
    public UserClientDTO createUserClient(CreateClientCTO userClientCTO) throws DuplicateResourceException {
        Client client = userClientMapper.toClientFromCreate(userClientCTO);
        User user = userClientMapper.toUserFromCreate(client, userClientCTO);
        Client found = clientRepository.findByClientId(client.getClientId());
        if (found != null)
            throw new DuplicateResourceException(null, "The client " + userClientCTO.getClientId() + " exist");
        Client savedClient = clientRepository.save(client);
        User savedUser = userRepository.save(user);

        UserClientDTO userClientDTO = new UserClientDTO();
        userClientDTO.setClientId(savedClient.getClientId());
        userClientDTO.setEnabled(savedUser.isEnabled());
        userClientDTO.setSuperAdmin(savedUser.getSuperAdmin());
        userClientDTO.setName(savedUser.getFirstName());
        userClientDTO.setUsername(savedUser.getUsername());
        return userClientDTO;
    }

    @Transactional()
    @Override
    public UserClientDTO updateUserClient(UpdateClientCTO userClientCTO) throws ResourceNotFoundException, ValidationException {
        Client byClientId = clientRepository.findByClientId(userClientCTO.getClientId());
        if (byClientId == null)
            throw new ResourceNotFoundException(null, "Client not found ");

        List<User> users = userRepository.fetchByClientIdAndIsSuperAdmin(byClientId.getClientId());
        if (users.isEmpty()) {
            throw new ResourceNotFoundException(null, "User of client not found ");
        }
        User byUser = users.get(0);
        if (userClientCTO.getNewPassword() != null) {
            UserPassword data = new UserPassword();
            data.setConfirmPassword(userClientCTO.getConfirmPassword());
            data.setNewPassword(userClientCTO.getNewPassword());
            userService.changePassword(byUser, data);
            byClientId.setClientSecret(passwordEncoder.encode(userClientCTO.getNewPassword()));
            clientRepository.save(byClientId);
        }
        if (userClientCTO.getName() != null)
            byUser.setFirstName(userClientCTO.getName());

        if (userClientCTO.getEnabled() != null)
            byUser.setEnabled(userClientCTO.getEnabled());
        User savedUser = userRepository.save(byUser);

        UserClientDTO userClientDTO = new UserClientDTO();
        userClientDTO.setClientId(userClientCTO.getClientId());
        userClientDTO.setEnabled(savedUser.isEnabled());
        userClientDTO.setSuperAdmin(savedUser.getSuperAdmin());
        userClientDTO.setName(savedUser.getFirstName());
        userClientDTO.setUsername(savedUser.getUsername());
        return userClientDTO;
    }
}
