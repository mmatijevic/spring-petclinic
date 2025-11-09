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

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.system.SecurityConfig;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link VetController}
 */

@WebMvcTest(VetController.class)
@Import(SecurityConfig.class)
@DisabledInNativeImage
@DisabledInAotMode
class VetControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VetRepository vets;

	@MockitoBean
	private SpecialtyRepository specialties;

	private Vet james() {
		Vet james = new Vet();
		james.setFirstName("James");
		james.setLastName("Carter");
		james.setId(1);
		return james;
	}

	private Vet helen() {
		Vet helen = new Vet();
		helen.setFirstName("Helen");
		helen.setLastName("Leary");
		helen.setId(2);
		Specialty radiology = new Specialty();
		radiology.setId(1);
		radiology.setName("radiology");
		helen.addSpecialty(radiology);
		return helen;
	}

	private Vet linda() {
		Vet linda = new Vet();
		linda.setFirstName("Linda");
		linda.setLastName("Douglas");
		linda.setId(3);
		Specialty dentistry = new Specialty();
		dentistry.setId(2);
		dentistry.setName("dentistry");
		linda.addSpecialty(dentistry);
		Specialty surgery = new Specialty();
		surgery.setId(3);
		surgery.setName("surgery");
		linda.addSpecialty(surgery);
		return linda;
	}

	@BeforeEach
	void setup() {
		given(this.vets.findAll()).willReturn(Lists.newArrayList(james(), helen(), linda()));
		given(this.vets.findAll(any(Pageable.class)))
			.willReturn(new PageImpl<Vet>(Lists.newArrayList(james(), helen(), linda())));
		given(this.specialties.findAll()).willReturn(Lists.newArrayList());
	}

	@Test
	void testShowVetListHtml() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/vets.html?page=1"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(view().name("vets/vetList"));

	}

	@Test
	void testShowResourcesVetList() throws Exception {
		ResultActions actions = mockMvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
		actions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.vetList[0].id").value(1));
	}

	@Test
	void testSearchVetsByName() throws Exception {
		// Mock the search to return only Helen when searching for "Helen"
		given(this.vets.searchVets(any(String.class), any(), any(Pageable.class)))
			.willReturn(new PageImpl<Vet>(Lists.newArrayList(helen())));

		mockMvc.perform(get("/vets.html?page=1&name=Helen"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attribute("name", "Helen"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testSearchVetsBySpecialty() throws Exception {
		// Mock the search to return only Helen when searching for "radiology"
		given(this.vets.searchVets(any(), any(String.class), any(Pageable.class)))
			.willReturn(new PageImpl<Vet>(Lists.newArrayList(helen())));

		mockMvc.perform(get("/vets.html?page=1&specialty=radiology"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attribute("specialty", "radiology"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testSearchVetsByNameAndSpecialty() throws Exception {
		// Mock the search to return Helen when searching for both name and specialty
		given(this.vets.searchVets(any(String.class), any(String.class), any(Pageable.class)))
			.willReturn(new PageImpl<Vet>(Lists.newArrayList(helen())));

		mockMvc.perform(get("/vets.html?page=1&name=Leary&specialty=radiology"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attribute("name", "Leary"))
			.andExpect(model().attribute("specialty", "radiology"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testSearchVetsWithNoResults() throws Exception {
		// Mock the search to return empty list when no matches found
		given(this.vets.searchVets(any(String.class), any(), any(Pageable.class)))
			.willReturn(new PageImpl<Vet>(Lists.newArrayList()));

		mockMvc.perform(get("/vets.html?page=1&name=NonExistent"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attribute("name", "NonExistent"))
			.andExpect(view().name("vets/vetList"));
	}

}
