package com.kurly.cloud.point.api.member.repository;

import com.kurly.cloud.point.api.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByMemberId(String memberId);
}
