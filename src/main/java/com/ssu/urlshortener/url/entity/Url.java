package com.ssu.urlshortener.url.entity;

import java.time.LocalDateTime;

import com.ssu.urlshortener.global.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Url extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(nullable = false, unique = true, length = 20)
    private String shortCode;

    @Column(nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    private LocalDateTime expiresAt;

    public void increaseClickCount() {
        clickCount++;
    }

}