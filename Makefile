clear:
	lein clean
	rm -rf target

build: clean
	lein uberjar

publish:
	docker push michaelstephan/bla

deploy:
	cf push -o michaelstephan/bla -f manifest.yaml
