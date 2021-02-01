package fr.be.your.self.repository;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface BaseRepository<T, K extends Serializable> extends PagingAndSortingRepository<T, K> {

}
