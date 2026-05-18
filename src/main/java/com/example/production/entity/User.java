package com.example.production.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "email", columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "password", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String password;

    @Column(name = "first_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String firstName;

    @Column(name = "last_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String lastName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    private LocalDate birthday;

    @Column(columnDefinition = "NVARCHAR(20)")
    private String phone;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String avatar;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    @BatchSize(size = 20)
    @Builder.Default
    private List<UserHashRole> userHasRoles = new java.util.ArrayList<>();
    // ----------------------
    // Spring Security UserDetails
    // ----------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (userHasRoles == null) {
            return List.of();
        }

        return userHasRoles.stream()
                .filter(userHashRole -> userHashRole.getRole() != null)
                .map(userHashRole -> new SimpleGrantedAuthority(userHashRole.getRole().getName()))
                .toList();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // luôn true, bạn có thể đổi logic nếu muốn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
