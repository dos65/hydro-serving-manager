INSERT INTO hydro_serving.runtime (runtime_id, "name", version, tags, config_params) VALUES
  (1000,'hydro-serving/test','0.0.1','{"python","code","test"}','{"test"}'),
  (1001,'hydro-serving/test','0.0.2','{"python","test"}','{"test"}'),
  (1002,'hydro-serving/test2','0.0.1','{"code","test"}','{"test2"}');


INSERT INTO hydro_serving.model (model_id, "name", source,
  model_contract, description, created_timestamp, updated_timestamp)  VALUES
  (1000, 'Test','itsource:/model','','desc','2004-10-19 10:23:54','2004-10-19 10:23:54');