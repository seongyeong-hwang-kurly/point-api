package com.kurly.cloud.point.api.member.repository;

import com.kurly.cloud.point.api.member.entity.DormantMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DormantMemberRepository extends JpaRepository<DormantMember, Long> {
  Optional<DormantMember> findByMemberId(String memberId);
}
