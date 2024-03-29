package fr.esgi.pa.server.code.infrastructure.device;

import fr.esgi.pa.server.code.core.Compiler;
import fr.esgi.pa.server.language.core.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompilerRepositoryImpl implements CompilerRepository {
    private final ApplicationContext context;

    @Override
    public Compiler findByLanguage(Language language) {
        return context.getBean(CCompiler.class);
    }
}
