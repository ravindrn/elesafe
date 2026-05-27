package com.elephant.safetybackend.repository;

import com.elephant.safetybackend.model.NewsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<NewsItem, Long> {

    List<NewsItem> findByIsActiveTrueOrderByPublishedDateDesc();

    List<NewsItem> findByTypeAndIsActiveTrueOrderByPublishedDateDesc(String type);

    List<NewsItem> findByIsActiveTrueAndTypeInOrderByPublishedDateDesc(List<String> types);
}