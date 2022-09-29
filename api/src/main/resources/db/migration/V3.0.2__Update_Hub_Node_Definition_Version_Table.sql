UPDATE `jm_hub_node_definition_version`
SET `input_parameters`  = Replace(`input_parameters`, 'java.util.HashSet', 'java.util.ArrayList'),
    `output_parameters` = Replace(`output_parameters`, 'java.util.HashSet', 'java.util.ArrayList');
