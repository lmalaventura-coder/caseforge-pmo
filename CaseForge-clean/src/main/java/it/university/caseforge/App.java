package it.university.caseforge;

import it.university.caseforge.controller.InvestigationController;
import it.university.caseforge.factory.DemoCaseFactory;
import it.university.caseforge.model.DeductionEngine;
import it.university.caseforge.model.StrictAccusationEvaluationStrategy;
import it.university.caseforge.persistence.InMemoryCaseRepository;
import it.university.caseforge.view.JavaFxCaseView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        JavaFxCaseView view = new JavaFxCaseView();
        InvestigationController controller = new InvestigationController(
                new DemoCaseFactory(),
                new InMemoryCaseRepository(),
                new DeductionEngine(new StrictAccusationEvaluationStrategy()),
                view
        );

        view.bindController(controller);
        controller.loadDefaultCase();

        Scene scene = new Scene(view.getRoot(), 1280, 820);
        scene.getStylesheets().add(Objects.requireNonNull(
                App.class.getResource("/it/university/caseforge/caseforge-dark.css")
        ).toExternalForm());
        stage.setTitle("CaseForge");
        stage.setMinWidth(1080);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }
}
