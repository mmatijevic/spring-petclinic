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

package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit tests for {@link Owner}.
 */
class OwnerTests {

	private Owner owner;

	@BeforeEach
	void setUp() {
		owner = new Owner();
	}

	// --- addVisit ---

	@Test
	void addVisitAttachesToCorrectPet() {
		Pet pet = new Pet();
		pet.setName("Buddy");
		owner.addPet(pet); // add while new (no id yet)
		pet.setId(1); // simulate persistence

		Visit visit = new Visit();
		visit.setDescription("annual checkup");

		owner.addVisit(1, visit);

		assertThat(pet.getVisits()).containsExactly(visit);
	}

	@Test
	void addVisitThrowsForNullPetId() {
		assertThatIllegalArgumentException().isThrownBy(() -> owner.addVisit(null, new Visit()))
			.withMessageContaining("Pet identifier must not be null");
	}

	@Test
	void addVisitThrowsForNullVisit() {
		Pet pet = new Pet();
		owner.addPet(pet); // add while new
		pet.setId(1); // simulate persistence

		assertThatIllegalArgumentException().isThrownBy(() -> owner.addVisit(1, null))
			.withMessageContaining("Visit must not be null");
	}

	@Test
	void addVisitThrowsForUnknownPetId() {
		assertThatIllegalArgumentException().isThrownBy(() -> owner.addVisit(99, new Visit()))
			.withMessageContaining("Invalid Pet identifier");
	}

	// --- getPet(String, boolean) ignoreNew behaviour ---

	@Test
	void getPetByNameIgnoreNewSkipsUnsavedPet() {
		Pet unsaved = new Pet();
		unsaved.setName("Max"); // id is null → isNew() == true
		owner.addPet(unsaved);

		// ignoreNew=true should not return the unsaved pet
		assertThat(owner.getPet("Max", true)).isNull();
		// ignoreNew=false (default) should still find it
		assertThat(owner.getPet("Max", false)).isSameAs(unsaved);
	}

	@Test
	void getPetByNameIsCaseInsensitive() {
		Pet pet = new Pet();
		pet.setName("fluffy");
		owner.addPet(pet); // add while new, then persist
		pet.setId(2);

		assertThat(owner.getPet("FLUFFY")).isSameAs(pet);
		assertThat(owner.getPet("Fluffy")).isSameAs(pet);
	}

}
