package com.capstone.exff.services;

import com.capstone.exff.entities.RoleEntity;
import com.capstone.exff.entities.UserEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserServices {
    ResponseEntity login(String phoneNumber, String password);
    ResponseEntity register(String phoneNumber, String password, String fullname, char status);
    ResponseEntity register(String phoneNumber, String password, String fullname, char status, RoleEntity roleId);
    ResponseEntity getAllUser();

    List<UserEntity> findUsersByName(String name);
    UserEntity findUserByPhone(String phone);
}
