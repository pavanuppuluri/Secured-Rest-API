package org.secureapp.service;

import org.secureapp.model.Role;

public interface RoleService {
    Role findByName(String name);
}
