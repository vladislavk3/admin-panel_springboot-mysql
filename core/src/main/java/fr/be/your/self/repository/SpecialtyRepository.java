package fr.be.your.self.repository;

import org.springframework.stereotype.Repository;

import fr.be.your.self.model.Specialty;

@Repository
public interface SpecialtyRepository extends BaseRepository<Specialty, Integer>  {

}
