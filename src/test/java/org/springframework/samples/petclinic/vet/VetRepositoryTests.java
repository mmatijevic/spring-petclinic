/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link VetRepository}.
 */
@DataJpaTest
class VetRepositoryTests {

	@Autowired
	private VetRepository vetRepository;

	@Test
	void testSearchVetsByFirstName() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> result = vetRepository.searchVets("James", null, pageable);

		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent()).anyMatch(vet -> vet.getFirstName().toLowerCase().contains("james"));
	}

	@Test
	void testSearchVetsByLastName() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> result = vetRepository.searchVets("Carter", null, pageable);

		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent()).anyMatch(vet -> vet.getLastName().toLowerCase().contains("carter"));
	}

	@Test
	void testSearchVetsBySpecialty() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> result = vetRepository.searchVets(null, "radiology", pageable);

		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent()).allMatch(vet -> vet.getSpecialties()
			.stream()
			.anyMatch(specialty -> specialty.getName().toLowerCase().contains("radiology")));
	}

	@Test
	void testSearchVetsByNameAndSpecialty() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> result = vetRepository.searchVets("Helen", "radiology", pageable);

		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent()).allMatch(vet -> {
			boolean nameMatch = vet.getFirstName().toLowerCase().contains("helen")
					|| vet.getLastName().toLowerCase().contains("helen");
			boolean specialtyMatch = vet.getSpecialties()
				.stream()
				.anyMatch(specialty -> specialty.getName().toLowerCase().contains("radiology"));
			return nameMatch && specialtyMatch;
		});
	}

	@Test
	void testSearchVetsCaseInsensitive() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> result1 = vetRepository.searchVets("JAMES", null, pageable);
		Page<Vet> result2 = vetRepository.searchVets("james", null, pageable);

		assertThat(result1.getTotalElements()).isEqualTo(result2.getTotalElements());
	}

	@Test
	void testSearchVetsWithPartialName() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> result = vetRepository.searchVets("am", null, pageable);

		// Should find "James" which contains "am"
		assertThat(result.getContent()).isNotEmpty();
	}

	@Test
	void testSearchVetsWithNoMatch() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> result = vetRepository.searchVets("NonExistentName", null, pageable);

		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void testSearchVetsWithNullParameters() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> resultAll = vetRepository.findAll(pageable);
		Page<Vet> resultSearch = vetRepository.searchVets(null, null, pageable);

		// When both parameters are null, should return all vets
		assertThat(resultSearch.getTotalElements()).isEqualTo(resultAll.getTotalElements());
	}

	@Test
	void testSearchVetsWithSpecialtyNoVetHas() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> result = vetRepository.searchVets(null, "NonExistentSpecialty", pageable);

		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void testSearchVetsPagination() {
		// Test that pagination works correctly
		Pageable firstPage = PageRequest.of(0, 2);
		Pageable secondPage = PageRequest.of(1, 2);

		Page<Vet> page1 = vetRepository.searchVets(null, null, firstPage);
		Page<Vet> page2 = vetRepository.searchVets(null, null, secondPage);

		assertThat(page1.getContent()).hasSizeLessThanOrEqualTo(2);
		assertThat(page2.getContent()).hasSizeLessThanOrEqualTo(2);

		// Ensure pages contain different vets if there are enough vets
		if (page1.getContent().size() == 2 && page2.getContent().size() > 0) {
			assertThat(page1.getContent()).doesNotContainAnyElementsOf(page2.getContent());
		}
	}

}
