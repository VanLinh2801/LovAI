package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.couple.CreateCoupleRequest;
import com.lovai.lovaiapi.dto.couple.CreateCoupleWithPartnerRequest;
import com.lovai.lovaiapi.dto.couple.CreateCoupleInviteRequest;
import com.lovai.lovaiapi.dto.couple.CoupleResponse;
import com.lovai.lovaiapi.dto.couple.CoupleInviteResponse;
import com.lovai.lovaiapi.dto.couple.UpdateCoupleRequest;
import com.lovai.lovaiapi.dto.user.UserResponse;
import com.lovai.lovaiapi.model.Couple;
import com.lovai.lovaiapi.model.User;
import com.lovai.lovaiapi.model.PartnerProfile;
import com.lovai.lovaiapi.model.CoupleInvite;
import com.lovai.lovaiapi.repository.CoupleRepository;
import com.lovai.lovaiapi.repository.UserRepository;
import com.lovai.lovaiapi.repository.PartnerProfileRepository;
import com.lovai.lovaiapi.repository.CoupleInviteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final PartnerProfileRepository partnerProfileRepository;
    private final CoupleInviteRepository coupleInviteRepository;

    public CoupleService(CoupleRepository coupleRepository, UserRepository userRepository, PartnerProfileRepository partnerProfileRepository, CoupleInviteRepository coupleInviteRepository) {
        this.coupleRepository = coupleRepository;
        this.userRepository = userRepository;
        this.partnerProfileRepository = partnerProfileRepository;
        this.coupleInviteRepository = coupleInviteRepository;
    }

    @Transactional
    public CoupleResponse createCouple(CreateCoupleRequest request) {
        User user1 = userRepository.findById(request.getUser1Id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User 1 không tồn tại"));

        User user2 = userRepository.findById(request.getUser2Id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User 2 không tồn tại"));

        if (coupleRepository.existsByUser1OrUser2AndDeletedAtIsNull(user1, user1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User 1 đã có couple");
        }

        if (coupleRepository.existsByUser1OrUser2AndDeletedAtIsNull(user2, user2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User 2 đã có couple");
        }

        if (user1.getId().equals(user2.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể tạo couple với chính mình");
        }

        List<Couple> deletedCouples = coupleRepository.findByUser1AndUser2AndDeletedAtIsNotNull(user1, user2);
        
        Couple couple;
        if (!deletedCouples.isEmpty()) {
            couple = deletedCouples.get(0); // Lấy couple gần nhất
            couple.setDeletedAt(null); // Khôi phục couple
            couple.setTitle(request.getTitle());
            couple.setAnniversaryDate(request.getAnniversaryDate());
            
            Map<String, Object> meta = couple.getMeta();
            if (meta == null) {
                meta = new HashMap<>();
            }
            meta.put("status", "active");
            meta.put("restored_at", java.time.OffsetDateTime.now().toString());
            meta.put("restored_by", user1.getId().toString());
            couple.setMeta(meta);
        } else {
            couple = new Couple();
            couple.setTitle(request.getTitle());
            couple.setAnniversaryDate(request.getAnniversaryDate());
            couple.setUser1(user1);
            couple.setUser2(user2);
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("status", "active");
            meta.put("created_by", user1.getId().toString());
            couple.setMeta(meta);
        }

        Couple savedCouple = coupleRepository.save(couple);

        return convertToCoupleResponse(savedCouple);
    }

    private CoupleResponse convertToCoupleResponse(Couple couple) {
        CoupleResponse response = new CoupleResponse();
        response.setId(couple.getId());
        response.setTitle(couple.getTitle());
        response.setAnniversaryDate(couple.getAnniversaryDate());
        response.setMeta(couple.getMeta());
        response.setCreatedAt(couple.getCreatedAt());
        response.setUpdatedAt(couple.getUpdatedAt());

        UserResponse user1Response = new UserResponse();
        user1Response.setId(couple.getUser1().getId());
        user1Response.setEmail(couple.getUser1().getEmail());
        user1Response.setName(couple.getUser1().getName());
        user1Response.setGender(couple.getUser1().getGender());
        user1Response.setDateOfBirth(couple.getUser1().getDateOfBirth());
        response.setUser1(user1Response);

        if (couple.getUser2() != null) {
            UserResponse user2Response = new UserResponse();
            user2Response.setId(couple.getUser2().getId());
            user2Response.setEmail(couple.getUser2().getEmail());
            user2Response.setName(couple.getUser2().getName());
            user2Response.setGender(couple.getUser2().getGender());
            user2Response.setDateOfBirth(couple.getUser2().getDateOfBirth());
            response.setUser2(user2Response);
        }

        return response;
    }

    @Transactional
    public CoupleResponse createCoupleWithPartner(CreateCoupleWithPartnerRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User không tồn tại"));

        if (coupleRepository.existsByUser1OrUser2AndDeletedAtIsNull(user, user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User đã có couple");
        }

        PartnerProfile partnerProfile = new PartnerProfile();
        partnerProfile.setUser(user); // Liên kết với user hiện tại
        partnerProfile.setName(request.getPartner().getName());
        partnerProfile.setGender(request.getPartner().getGender());
        partnerProfile.setDateOfBirth(request.getPartner().getDateOfBirth());
        partnerProfile.setPhone(request.getPartner().getPhone());
        partnerProfile.setEmail(request.getPartner().getEmail());
        partnerProfile.setNotes(request.getPartner().getNotes());
        
        Map<String, Object> partnerMeta = new HashMap<>();
        partnerMeta.put("created_by", user.getId().toString());
        partnerMeta.put("created_for", "couple");
        partnerProfile.setMeta(partnerMeta);

        PartnerProfile savedPartnerProfile = partnerProfileRepository.save(partnerProfile);

        Couple couple = new Couple();
        couple.setTitle(request.getTitle());
        couple.setAnniversaryDate(request.getAnniversaryDate());
        couple.setUser1(user);
        couple.setPartnerProfile(savedPartnerProfile);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("status", "active");
        meta.put("created_by", user.getId().toString());
        meta.put("partner_type", "profile");
        couple.setMeta(meta);

        Couple savedCouple = coupleRepository.save(couple);

        return convertToCoupleResponseWithPartner(savedCouple);
    }

    public CoupleResponse getCoupleByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User không tồn tại"));

        List<Couple> couples = coupleRepository.findByUser1OrUser2AndDeletedAtIsNull(user, user);
        
        if (couples.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User chưa có couple");
        }

        Couple couple = couples.get(0);

        return convertToCoupleResponse(couple);
    }

    @Transactional
    public CoupleResponse updateCouple(UUID coupleId, UpdateCoupleRequest request) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple không tồn tại"));

        if (couple.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple đã bị xóa");
        }

        couple.setTitle(request.getTitle());
        couple.setAnniversaryDate(request.getAnniversaryDate());

        if (request.getPartner() != null && couple.getPartnerProfile() != null) {
            PartnerProfile partnerProfile = couple.getPartnerProfile();
            partnerProfile.setName(request.getPartner().getName());
            partnerProfile.setGender(request.getPartner().getGender());
            partnerProfile.setDateOfBirth(request.getPartner().getDateOfBirth());
            partnerProfile.setPhone(request.getPartner().getPhone());
            partnerProfile.setEmail(request.getPartner().getEmail());
            partnerProfile.setNotes(request.getPartner().getNotes());
            
            Map<String, Object> partnerMeta = partnerProfile.getMeta();
            if (partnerMeta == null) {
                partnerMeta = new HashMap<>();
            }
            partnerMeta.put("updated_at", java.time.OffsetDateTime.now().toString());
            partnerProfile.setMeta(partnerMeta);
            
            partnerProfileRepository.save(partnerProfile);
        }

        Map<String, Object> meta = couple.getMeta();
        if (meta == null) {
            meta = new HashMap<>();
        }
        meta.put("updated_at", java.time.OffsetDateTime.now().toString());
        couple.setMeta(meta);

        Couple savedCouple = coupleRepository.save(couple);

        return convertToCoupleResponse(savedCouple);
    }

    private CoupleResponse convertToCoupleResponseWithPartner(Couple couple) {
        CoupleResponse response = new CoupleResponse();
        response.setId(couple.getId());
        response.setTitle(couple.getTitle());
        response.setAnniversaryDate(couple.getAnniversaryDate());
        response.setMeta(couple.getMeta());
        response.setCreatedAt(couple.getCreatedAt());
        response.setUpdatedAt(couple.getUpdatedAt());

        UserResponse user1Response = new UserResponse();
        user1Response.setId(couple.getUser1().getId());
        user1Response.setEmail(couple.getUser1().getEmail());
        user1Response.setName(couple.getUser1().getName());
        user1Response.setGender(couple.getUser1().getGender());
        user1Response.setDateOfBirth(couple.getUser1().getDateOfBirth());
        response.setUser1(user1Response);

        if (couple.getPartnerProfile() != null) {
            UserResponse partnerResponse = new UserResponse();
            partnerResponse.setId(couple.getPartnerProfile().getId());
            partnerResponse.setEmail(couple.getPartnerProfile().getEmail());
            partnerResponse.setName(couple.getPartnerProfile().getName());
            partnerResponse.setGender(couple.getPartnerProfile().getGender());
            partnerResponse.setDateOfBirth(couple.getPartnerProfile().getDateOfBirth());
            response.setUser2(partnerResponse);
        }

        return response;
    }

    @Transactional
    public CoupleInviteResponse createCoupleInvite(UUID inviterId, CreateCoupleInviteRequest request) {
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người mời không tồn tại"));

        User invitee = userRepository.findById(request.getInviteeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người được mời không tồn tại"));

        if (inviter.getId().equals(invitee.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể mời chính mình");
        }

        if (coupleRepository.existsByUser1OrUser2AndDeletedAtIsNull(inviter, inviter)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bạn đã có couple");
        }

        if (coupleRepository.existsByUser1OrUser2AndDeletedAtIsNull(invitee, invitee)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Người được mời đã có couple");
        }

        if (coupleInviteRepository.existsActiveInviteBetweenUsers(inviter, invitee, OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đã có lời mời chưa hết hạn giữa 2 người");
        }

        CoupleInvite invite = new CoupleInvite();
        invite.setInviter(inviter);
        invite.setInvitee(invitee);
        
        if (request.getExpiresAt() != null) {
            invite.setExpiresAt(request.getExpiresAt());
        } else {
            invite.setExpiresAt(OffsetDateTime.now().plusDays(7));
        }

        CoupleInvite savedInvite = coupleInviteRepository.save(invite);

        return convertToCoupleInviteResponse(savedInvite);
    }

    @Transactional
    public void leaveCouple(UUID coupleId, UUID userId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple không tồn tại"));

        if (couple.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple đã bị xóa");
        }

        if (!couple.getUser1().getId().equals(userId) && 
            (couple.getUser2() == null || !couple.getUser2().getId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền rời couple này");
        }

        couple.setDeletedAt(OffsetDateTime.now());
        
        Map<String, Object> meta = couple.getMeta();
        if (meta == null) {
            meta = new HashMap<>();
        }
        meta.put("status", "deleted");
        meta.put("deleted_at", OffsetDateTime.now().toString());
        meta.put("deleted_by", userId.toString());
        couple.setMeta(meta);

        coupleRepository.save(couple);
    }

    private CoupleInviteResponse convertToCoupleInviteResponse(CoupleInvite invite) {
        CoupleInviteResponse response = new CoupleInviteResponse();
        response.setId(invite.getId());
        response.setExpiresAt(invite.getExpiresAt());
        response.setCreatedAt(invite.getCreatedAt());
        response.setRespondedAt(invite.getRespondedAt());

        UserResponse inviterResponse = new UserResponse();
        inviterResponse.setId(invite.getInviter().getId());
        inviterResponse.setEmail(invite.getInviter().getEmail());
        inviterResponse.setName(invite.getInviter().getName());
        inviterResponse.setGender(invite.getInviter().getGender());
        inviterResponse.setDateOfBirth(invite.getInviter().getDateOfBirth());
        response.setInviter(inviterResponse);

        UserResponse inviteeResponse = new UserResponse();
        inviteeResponse.setId(invite.getInvitee().getId());
        inviteeResponse.setEmail(invite.getInvitee().getEmail());
        inviteeResponse.setName(invite.getInvitee().getName());
        inviteeResponse.setGender(invite.getInvitee().getGender());
        inviteeResponse.setDateOfBirth(invite.getInvitee().getDateOfBirth());
        response.setInvitee(inviteeResponse);

        return response;
    }
}
