package com.pbshop.springshop.community;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByActiveTrueOrderByIdAsc();
}
