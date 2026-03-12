package org.joint.modules.system.user;

import org.joint.common.exception.BusinessException;
import org.joint.common.exception.ErrorCode;
import org.joint.modules.system.user.dto.CreateUserDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final Map<String, Map<String, Object>> users = new HashMap<>();

    public Map<String, Object> findById(String id) {
        Map<String, Object> user = users.get(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public List<Map<String, Object>> findAll(Integer page, Integer size) {
        return new ArrayList<>(users.values());
    }

    public Map<String, Object> create(CreateUserDto dto) {
        boolean exists = users.values().stream()
                .anyMatch(u -> dto.getUsername().equals(u.get("username")));
        if (exists) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        String id = UUID.randomUUID().toString();
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("username", dto.getUsername());
        user.put("nickName", dto.getNickName());
        user.put("email", dto.getEmail());
        user.put("phone", dto.getPhone());
        user.put("status", dto.getStatus());
        user.put("createdAt", new Date());
        users.put(id, user);
        return user;
    }

    public void delete(String id) {
        if (!users.containsKey(id)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        users.remove(id);
    }
}
