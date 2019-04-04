package com.capstone.exff.controllers;

import com.capstone.exff.constants.ExffStatus;
import com.capstone.exff.entities.RelationshipEntity;
import com.capstone.exff.entities.UserEntity;
import com.capstone.exff.repositories.RelationshipRepository;
import com.capstone.exff.services.RelationshipServices;
import com.capstone.exff.utilities.ExffMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class RelationshipController {

    private final RelationshipServices relationshipServices;

    @Autowired
    public RelationshipController(RelationshipServices relationshipServices) {
        this.relationshipServices = relationshipServices;
    }


    @GetMapping("/relationship/accepted")
    public ResponseEntity getAcceptedFriendRequestByUserId(@RequestAttribute("USER_INFO") UserEntity userEntity) {
        List<RelationshipEntity> relationshipEntities;
        try {
            int userId = userEntity.getId();
            relationshipEntities = relationshipServices.getAcceptedFriendRequestByUserId(userId);
        } catch (Exception e) {
            return new ResponseEntity(new ExffMessage("Cannot create relationship request"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(relationshipEntities, HttpStatus.OK);
    }

    @GetMapping("/relationship/friend")
    public ResponseEntity getFriendsByUserId(@RequestAttribute("USER_INFO") UserEntity userEntity) {
        List<RelationshipEntity> relationshipEntities;
        List<UserEntity> friendList = new ArrayList<>();
        try {
            int userId = userEntity.getId();
            relationshipEntities = relationshipServices.getFriendsByUserId(userId);

            for (int i = 0; i < relationshipEntities.size(); i++) {
                if (relationshipEntities.get(i).getSenderId() == userId) {
                    friendList.add(relationshipEntities.get(i).getReceiver());
                } else {
                    friendList.add(relationshipEntities.get(i).getSender());
                }
            }
        } catch (Exception e) {
            return new ResponseEntity(new ExffMessage("Cannot get relationship"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(friendList, HttpStatus.OK);
    }

    @GetMapping("/relationship/friend/count")
    public ResponseEntity countFriendsByUserId(@RequestAttribute("USER_INFO") UserEntity userEntity) {
        int count;
        try {
            int userId = userEntity.getId();
            count = relationshipServices.countFriendsByUserId(userId);
        } catch (Exception e) {
            return new ResponseEntity(new ExffMessage("Cannot count friend"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(count, HttpStatus.OK);
    }

    @GetMapping("/relationship")
    public ResponseEntity getRequestAddRelationship(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestAttribute("USER_INFO") UserEntity userEntity
    ) {
        List<RelationshipEntity> relationshipEntities;
        try {
            int userId = userEntity.getId();
            Pageable pageable = PageRequest.of(page, size);
            relationshipEntities = relationshipServices.getAddRelationshipRequest(userId, pageable);
        } catch (Exception e) {
            return new ResponseEntity(new ExffMessage("Cannot create relationship request"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(relationshipEntities, HttpStatus.OK);
    }

    @PostMapping("/relationship")
    public ResponseEntity requestAddRelationship(@RequestBody Map<String, String> body, @RequestAttribute("USER_INFO") UserEntity userEntity) {
        try {
            int senderId = userEntity.getId();
            int receiverId = Integer.parseInt(body.get("receiverId"));
            RelationshipEntity res = relationshipServices.sendAddRelationshipRequest(senderId, receiverId);
            if (res != null) {
                return new ResponseEntity(res, HttpStatus.OK);
            } else {
                return new ResponseEntity(new ExffMessage("Cannot create relationship request"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity(new ExffMessage("Cannot create relationship request"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/relationship")
    public ResponseEntity acceptRelationship(@RequestBody Map<String, String> body, @RequestAttribute("USER_INFO") UserEntity userEntity) {
        try {
            int id = Integer.parseInt(body.get("id"));
            int userId = userEntity.getId();
            boolean res = relationshipServices.acceptAddRelationshipRequest(id, userId);
            if (res) {
                return new ResponseEntity(new ExffMessage("Relationship request has been accepted"), HttpStatus.OK);
            } else {
                return new ResponseEntity(new ExffMessage("Cannot accept relationship request"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity(new ExffMessage("Cannot accept relationship request"), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("/relationship/check")
    public ResponseEntity checkRelationship(ServletRequest servletRequest, @RequestBody Map<String, String> body) {
        try {
            int senderId = getLoginUserId(servletRequest);
//            System.out.println("test senderID " + senderId);
            int receiverId = Integer.parseInt(body.get("receiverId"));
            RelationshipEntity res = relationshipServices.checkFriend(senderId, receiverId);
            if (res != null) {
                return new ResponseEntity(res, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity(new ExffMessage("Can not check"), HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/relationship")
    public ResponseEntity removeRelationship(ServletRequest servletRequest, @RequestBody Map<String, String> body){
        try {
            int senderId = getLoginUserId(servletRequest);
//            System.out.println("test senderID " + senderId);
            int id = Integer.parseInt(body.get("id"));
            boolean res = relationshipServices.removeRelationship(id, senderId);
            if (res) {
                return new ResponseEntity(new ExffMessage("Done"), HttpStatus.OK);
            } else {
                return new ResponseEntity(new ExffMessage("Fail"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity(new ExffMessage("Fail"), HttpStatus.BAD_REQUEST);
    }

    private int getLoginUserId(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        UserEntity userEntity = (UserEntity) request.getAttribute("USER_INFO");
        int userId = userEntity.getId();
        return userId;
    }

    @DeleteMapping("/relationship/{id:[\\d]+}")
    public ResponseEntity deleteRequest(ServletRequest servletRequest, @PathVariable("id") int id) {
        try {
            int loginUserId = getLoginUserId(servletRequest);
            RelationshipEntity relationshipEntity = relationshipServices.getRelationshipByRelationshipId(id);
            if (loginUserId == relationshipEntity.getReceiverId() || loginUserId == relationshipEntity.getSenderId()) {
                relationshipServices.deleteRelationship(relationshipEntity);
            } else {
                return new ResponseEntity(new ExffMessage("Not permission"), HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            return new ResponseEntity(new ExffMessage(e.getMessage()), HttpStatus.CONFLICT);
        }
        return new ResponseEntity(new ExffMessage("Deleted"), HttpStatus.OK);
    }
}
