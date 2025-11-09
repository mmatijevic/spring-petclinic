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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

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
		return linda;
	}

	@BeforeEach
	void setup() {
		given(this.vets.findAll()).willReturn(Lists.newArrayList(james(), helen(), linda()));
		given(this.vets.findAll(any(Pageable.class)))
			.willReturn(new PageImpl<Vet>(Lists.newArrayList(james(), helen(), linda())));
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
		// Setup: when searching for "Helen", return only Helen
		given(this.vets.searchVets(eq("Helen"), isNull(), any(Pageable.class)))
			.willReturn(new PageImpl<>(Lists.newArrayList(helen())));

		mockMvc.perform(get("/vets.html").param("page", "1").param("name", "Helen"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attribute("name", "Helen"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testSearchVetsBySpecialty() throws Exception {
		// Setup: when searching for "radiology", return only Helen
		given(this.vets.searchVets(isNull(), eq("radiology"), any(Pageable.class)))
			.willReturn(new PageImpl<>(Lists.newArrayList(helen())));

		mockMvc.perform(get("/vets.html").param("page", "1").param("specialty", "radiology"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attribute("specialty", "radiology"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testSearchVetsByNameAndSpecialty() throws Exception {
		// Setup: when searching for name "Helen" and specialty "radiology", return Helen
		given(this.vets.searchVets(eq("Helen"), eq("radiology"), any(Pageable.class)))
			.willReturn(new PageImpl<>(Lists.newArrayList(helen())));

		mockMvc.perform(get("/vets.html").param("page", "1").param("name", "Helen").param("specialty", "radiology"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attribute("name", "Helen"))
			.andExpect(model().attribute("specialty", "radiology"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testSearchVetsWithEmptyName() throws Exception {
		// Setup: when name is empty string, should search with null
		given(this.vets.searchVets(isNull(), eq("dentistry"), any(Pageable.class)))
			.willReturn(new PageImpl<>(Lists.newArrayList(linda())));

		mockMvc.perform(get("/vets.html").param("page", "1").param("name", "").param("specialty", "dentistry"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testSearchVetsNoResults() throws Exception {
		// Setup: when searching for non-existent vet, return empty list
		given(this.vets.searchVets(eq("NonExistent"), isNull(), any(Pageable.class)))
			.willReturn(new PageImpl<>(Lists.newArrayList()));

		mockMvc.perform(get("/vets.html").param("page", "1").param("name", "NonExistent"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attribute("name", "NonExistent"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testSearchParametersPreservedInModel() throws Exception {
		// Verify that search parameters are added to the model for form persistence
		given(this.vets.searchVets(eq("James"), eq("surgery"), any(Pageable.class)))
			.willReturn(new PageImpl<>(Lists.newArrayList()));

		mockMvc.perform(get("/vets.html").param("page", "1").param("name", "James").param("specialty", "surgery"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("name", "James"))
			.andExpect(model().attribute("specialty", "surgery"))
			.andExpect(view().name("vets/vetList"));
	}

}
