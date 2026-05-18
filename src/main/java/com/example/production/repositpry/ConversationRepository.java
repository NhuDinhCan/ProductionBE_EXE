package com.example.production.repositpry;

import com.example.production.entity.Conversation;
import com.example.production.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findFirstByUserIdAndMentorId(Long userId, Long mentorId);

    @Query(value = "SELECT TOP 1 * FROM conversation WHERE (user_id = :u1 AND mentor_id = :u2) OR (user_id = :u2 AND mentor_id = :u1)", nativeQuery = true)
    Optional<Conversation> findChatBetweenSafe(@Param("u1") Long user1, @Param("u2") Long user2);

    // Dùng native query vì Conversation không có @ManyToOne trực tiếp tới User
    @Query(value = "SELECT u.* FROM users u INNER JOIN conversation c ON u.id = c.user_id WHERE c.mentor_id = :mentorId", nativeQuery = true)
    List<User> findUsersByMentorId(@Param("mentorId") Long mentorId);
}
