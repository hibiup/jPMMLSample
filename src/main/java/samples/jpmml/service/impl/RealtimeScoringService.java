package samples.jpmml.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.PMMLObject;
import org.jpmml.evaluator.*;
import org.jpmml.evaluator.visitors.ElementInternerBattery;
import org.jpmml.evaluator.visitors.ElementOptimizerBattery;
import org.jpmml.model.VisitorBattery;
import org.jpmml.model.visitors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import samples.jpmml.service.RepositoryManager;

import java.text.NumberFormat;
import java.util.*;

@Service
public class RealtimeScoringService extends AbstractResourceManager implements RealtimeScoring<Map<String, Number>, Map<String, Object>> {
    Logger logger = LogManager.getLogger(RealtimeScoringService.class);

    @Autowired
    RepositoryManager repoManager;

    @Override
    public Map<String, Object> predict(ModelEvaluator evaluator, Map<String, Number> input) {
        Map<FieldName, FieldValue> arguments  = translateInput(evaluator.getActiveFields(), input);

        Map<FieldName, ?> results = evaluator.evaluate(arguments );
        List<TargetField> targetFields = evaluator.getTargetFields();

        return translaterOutput(targetFields, results);
    }

    Map<FieldName, FieldValue> translateInput(final List<InputField> fields, final Map<String, Number> input) {
        Map<FieldName, FieldValue> arguments  = new HashMap();
        fields.forEach(field -> {
            FieldValue fieldValue = field.prepare(input.get(field.getName().toString()));
            arguments .put(field.getName(), fieldValue);
        });

        return arguments;
    }

    Map<String, Object> translaterOutput(final List<TargetField> targetFields, final Map<FieldName, ?> results ) {
        Map<String, Object> output = new HashMap();
        for(TargetField targetField : targetFields ){
            FieldName targetFieldName = targetField.getName();
            Object targetFieldValue = results.get(targetFieldName);

            output.put(targetFieldName.toString(), targetFieldValue);
        }
        return output;
    }

    @Override
    public ModelEvaluator getModelEvaluator(String modelName) throws PMMLLoadingException{
        logger.debug("Model " + modelName + " is loading.");
        PMML pmmlModel = repoManager.getModel(modelName);

        applyVisitorBattery(pmmlModel);
        //measureModel(pmmlModel);

        ModelEvaluator evaluator = ModelEvaluatorFactory.newInstance().newModelEvaluator(pmmlModel);
        evaluator.verify();

        logger.debug("Model " + modelName + " is loaded.");
        return evaluator;
    }

    void applyVisitorBattery(PMML pmmlModel) {
        VisitorBattery visitorBattery = new VisitorBattery();
        visitorBattery.add(LocatorNullifier.class);
        visitorBattery.addAll(new ElementOptimizerBattery());
        visitorBattery.addAll(new AttributeInternerBattery());
        visitorBattery.addAll(new ElementInternerBattery());
        visitorBattery.add(ArrayListTransformer.class);
        visitorBattery.add(ArrayListTrimmer.class);
        visitorBattery.applyTo(pmmlModel);
    }

    void measureModel(PMML pmmlModel) {
        MemoryMeasurer memoryMeasurer = new MemoryMeasurer();
        memoryMeasurer.applyTo(pmmlModel);

        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setGroupingUsed(true);

        long size = memoryMeasurer.getSize();
        logger.debug("Bytesize of the object graph: " + numberFormat.format(size));

        Set<Object> objects = memoryMeasurer.getObjects();

        long objectCount = objects.size();

        logger.debug("Number of distinct Java objects in the object graph: " + numberFormat.format(objectCount));

        long pmmlObjectCount = objects.stream()
                .filter(PMMLObject.class::isInstance)
                .count();

        logger.debug("\t" + "PMML class model objects: " + numberFormat.format(pmmlObjectCount));
        logger.debug("\t" + "Other objects: " + numberFormat.format(objectCount - pmmlObjectCount));
    }
}
