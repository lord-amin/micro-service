package com.peykasa.authserver.controller;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.audit.aspect.Auditable;
import com.peykasa.authserver.audit.aspect.provider.GetExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.SearchProvider;
import com.peykasa.authserver.audit.aspect.provider.client.*;
import com.peykasa.authserver.controller.validator.UserClientValidator;
import com.peykasa.authserver.exception.PermissionDeniedException;
import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.model.cto.BaseCTO;
import com.peykasa.authserver.model.cto.CreateClientCTO;
import com.peykasa.authserver.model.cto.UpdateClientCTO;
import com.peykasa.authserver.model.dto.ClientDTO;
import com.peykasa.authserver.model.dto.SearchResult;
import com.peykasa.authserver.model.dto.UserClientDTO;
import com.peykasa.authserver.model.entity.Client;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.BaseRepository;
import com.peykasa.authserver.repository.ClientRepository;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@RequiredArgsConstructor
@RequestMapping(value = Constants.CLIENT_CONTEXT_PATH)
@RepositoryRestController
public class ClientController extends EntityController<ClientDTO, BaseCTO, Client, String> {
    protected final static Logger LOGGER = LoggerFactory.getLogger(ClientController.class);
    private final ClientRepository clientRepository;
    private final UserClientValidator userClientValidator;
    private final ClientService clientService;
    private final UserRepository userRepository;

    @Override
    public BaseRepository<Client, String> getRepository() {
        return clientRepository;
    }

    @Auditable(provider = SearchProvider.class, context = Constants.CLIENT, event = Constants.SEARCH)
    @GetMapping()
    @ResponseBody
    public PagedResources<Resource<ClientDTO>> findAll(String page, String size, String sort) {
        return super.findAll(page, size, sort);
    }

    @Auditable(provider = ClientSearchProvider.class, context = Constants.CLIENT, event = Constants.SEARCH)
    @GetMapping(path = "/list")
    @ResponseBody
    public SearchResult<UserClientDTO> findAllClientUser(String page, String size, String sort) {
        Pageable pageable = null;
        if (page != null && size != null && !"".equals(page) && !"".equals(size)) {
            pageable = new PageRequest(Integer.valueOf(page), Integer.valueOf(size), parseParameterIntoSort(new String[]{sort}));
        }
        Page<User> all = userRepository.findBySuperAdmin(pageable, true);
        List<UserClientDTO> list = new ArrayList<>();
        for (User user : all) {
            list.add(convert1(user));
        }
        return new SearchResult<>(list, all.getTotalElements());
    }

    @Auditable(provider = ClientGetProviderUser.class, exProvider = GetExceptionProvider.class, context = Constants.CLIENT, event = Constants.SEARCH)
    @GetMapping(path = "/user")
    @ResponseBody
    public UserClientDTO get(String clientId) throws ResourceNotFoundException {
        Client all = clientRepository.findByClientId(clientId);
        if (all == null)
            throw new ResourceNotFoundException(null, "The client " + clientId + " not found");
        List<User> users = userRepository.fetchByClientIdAndIsSuperAdmin(clientId);
        if (users.isEmpty()) {
            throw new ResourceNotFoundException(null, "User of client not found ");
        }
        User user = users.get(0);
        return convert1(user);
    }

    @Auditable(provider = ClientCreateProvider.class, exProvider = ClientCreateExceptionProvider.class, context = Constants.CLIENT, event = Constants.CREATE)
    @PostMapping()
    @ResponseBody
    public UserClientDTO createClient(@RequestBody CreateClientCTO userClientCTO) throws Exception {
        userClientValidator.getCreateUserClientValidator().validate(userClientCTO);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new ResourceNotFoundException(null, "Client not found ");
        return clientService.createUserClient(userClientCTO);

    }

    @Auditable(provider = ClientUpdateProvider.class, exProvider = ClientUpdateExceptionProvider.class, context = Constants.CLIENT, event = Constants.UPDATE)
    @PutMapping()
    @ResponseBody
    public UserClientDTO updateClient(@RequestBody UpdateClientCTO userClientCTO) throws Exception {
        userClientValidator.getUpdateUserClientValidator().validate(userClientCTO);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new ResourceNotFoundException(null, "Client not found ");
        if (!StringUtils.isEmpty(userClientCTO.getNewPassword())) {
            User principal = (User) authentication.getPrincipal();
            String loginClientId = principal.getClient().getClientId();
            String requestedClientId = userClientCTO.getClientId();
            if (requestedClientId.equals(loginClientId)) {
                throw new PermissionDeniedException("Could not change " + requestedClientId + " client password");
            }
        }
        return clientService.updateUserClient(userClientCTO);

    }

    private UserClientDTO convert1(User user) {
        UserClientDTO userClientDTO = new UserClientDTO();
        userClientDTO.setUsername(user.getUsername());
        userClientDTO.setName(user.getFirstName());
        userClientDTO.setSuperAdmin(user.getSuperAdmin());
        userClientDTO.setEnabled(user.isEnabled());
        userClientDTO.setCreationDate(user.getCreationDate());
        userClientDTO.setClientId(user.getClient().getClientId());
        return userClientDTO;
    }


    @Override
    protected List<ClientDTO> convert(Page<Client> all) {
        List<ClientDTO> list = new ArrayList<>();
        for (Client client : all.getContent()) {
            ClientDTO e = new ClientDTO();
            e.setClientId(client.getClientId());
            e.setClientSecret("****");
            e.setAccessTokenValiditySeconds(client.getAccessTokenValiditySeconds());
            e.setRefreshTokenValiditySeconds(client.getRefreshTokenValiditySeconds());
            e.setGrantType(client.getGrantType());
            list.add(e);
        }
        return list;
    }

    @Override
    protected Class<ClientDTO> support() {
        return ClientDTO.class;
    }

    @Override
    public String notFoundMessage() {
        return null;
    }

}