package massim.game.norms;

import java.util.Objects;
import java.util.function.Supplier;

public enum FactoryNorms {
    RoleIndividual(NormRoleIndividual::new),
    RoleTeam(NormRoleTeam::new),
    Block(NormCarry::new);

    public final Supplier<Norm> factory;
    private FactoryNorms(Supplier<Norm> factory) {
        this.factory = Objects.requireNonNull(factory);
    }
}