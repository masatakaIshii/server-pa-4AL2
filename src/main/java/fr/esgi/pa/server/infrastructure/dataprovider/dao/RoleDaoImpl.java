package fr.esgi.pa.server.infrastructure.dataprovider.dao;

import fr.esgi.pa.server.core.dao.RoleDao;
import fr.esgi.pa.server.core.exception.AlreadyCreatedException;
import fr.esgi.pa.server.core.model.Role;
import fr.esgi.pa.server.core.model.RoleName;
import fr.esgi.pa.server.infrastructure.dataprovider.entity.RoleEntity;
import fr.esgi.pa.server.infrastructure.dataprovider.mapper.RoleMapper;
import fr.esgi.pa.server.infrastructure.dataprovider.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleDaoImpl implements RoleDao {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public Long createRole(RoleName roleName) throws AlreadyCreatedException {
        checkIfRoleAlreadyExists(roleName);

        var roleToSave = new RoleEntity().setName(roleName);

        return roleRepository.save(roleToSave).getId();
    }

    private void checkIfRoleAlreadyExists(RoleName roleName) throws AlreadyCreatedException {
        var maybeRoleName = roleRepository.findByName(roleName);
        if (maybeRoleName.isPresent()) {
            var message = String.format("%s : role with name '%s' already exists", RoleDao.class, roleName);
            throw new AlreadyCreatedException(message);
        }
    }

    @Override
    public Optional<Role> findByRoleName(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .map(roleMapper::entityToDomain);
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::entityToDomain)
                .collect(Collectors.toList());
    }
}