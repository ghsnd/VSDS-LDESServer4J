package be.vlaanderen.informatievlaanderen.ldes.server.domain.validation;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.config.LdesConfig;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.exceptions.LdesShaclValidationException;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.tree.member.entities.Member;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LdesShaclValidatorTest {

	private LdesShaclValidator ldesShaclValidator;

	@BeforeEach
	void setUp() {
		LdesConfig ldesConfig = new LdesConfig();
		ldesConfig.validation().setShape("validation/example-shape.ttl");
		ldesConfig.validation().setEnabled(true);
		ldesShaclValidator = new LdesShaclValidator(ldesConfig);
	}

	@Test
	void when_ValidateProvidedValidData_thenReturnValid() throws URISyntaxException, IOException {
		Member validMember = readLdesMemberFromFile("validation/example-data.ttl");

		assertDoesNotThrow(() -> ldesShaclValidator.validateShape(validMember));
	}

	@Test
	void when_ValidateProvidedInvalidData_thenReturnInvalid() throws URISyntaxException, IOException {
		Member invalidMember = readLdesMemberFromFile("validation/example-data-invalid.ttl");

		assertThrows(LdesShaclValidationException.class, () -> ldesShaclValidator.validateShape(invalidMember));
	}

	@Test
	void when_ValidateWithNoProvidedShape_thenReturnValid() throws URISyntaxException, IOException {
		ldesShaclValidator = new LdesShaclValidator(new LdesConfig());

		Member validMember = readLdesMemberFromFile("validation/example-data.ttl");
		assertDoesNotThrow(() -> ldesShaclValidator.validateShape(validMember));

		Member invalidMember = readLdesMemberFromFile("validation/example-data-invalid.ttl");
		assertDoesNotThrow(() -> ldesShaclValidator.validateShape(invalidMember));
	}

	@Test
	void when_ValidateWithValidationNotEnabled_thenReturnValid() throws URISyntaxException, IOException {
		LdesConfig ldesConfig = new LdesConfig();
		ldesConfig.validation().setEnabled(false);
		ldesShaclValidator = new LdesShaclValidator(ldesConfig);

		Member validMember = readLdesMemberFromFile("validation/example-data.ttl");
		assertDoesNotThrow(() -> ldesShaclValidator.validateShape(validMember));

		Member invalidMember = readLdesMemberFromFile("validation/example-data-invalid.ttl");
		assertDoesNotThrow(() -> ldesShaclValidator.validateShape(invalidMember));
	}

	private Member readLdesMemberFromFile(String fileName)
			throws URISyntaxException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).toURI());

		Model m = RDFParserBuilder.create()
				.fromString(Files.lines(Paths.get(file.toURI())).collect(Collectors.joining())).lang(Lang.TTL)
				.toModel();
		return new Member("", m, new ArrayList<>());
	}
}
