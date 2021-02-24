## Weesner Development

This a repo for my server. It contains the services for the pet projects I have created. This and the front ends for
these services uses common resources like classes for each service that come from a Kotlin Multiplatform repo
[WeesnerDevelopment-Shared](https://github.com/adamWeesner/WeesnerDevelopment-Shared).

### [Bill Man](billMan)

This is the backend for the app I am working on called Bill Man... It allows you to keep track of your income and bill
spending habits. You can add `Income` and `Bill`s, each has at least on `Occurrence` associated with it. Each occurrence
has a "due date" when it is meant to end an "every" field that helps the service automatically create a new occurrence
when the due date is hit based on how often you wish to repeat it. Generally this would look something like having a
bill due every month, so you would have something like an occurrence with a due date that is set to a month in the
future (when the bill is due or when your check would hit) an every for it set to "1 Month".
[Swagger spec sheet](billMan/swagger.yaml).

### [Breath of the Wild](breathOfTheWild)

This is a service to give you back details about different things for the game Zelda: Breath of the Wild. Mostly for
things like cooking. Has endpoints for getting all available ingredients in the game, different cooking pot recipes,
effects, critters, etc. [Swagger spec sheet](breathOfTheWild/swagger.yaml).

### [Business Rules](businessRules)

This is the place where all the common things come in, things like classes, services and logging stuff that are common
amongst the different services but not needed outside them (like in the front-ends). This is also where the general
stuff for auth for all the services reside. They all use a single auth service to log in, so the beginnings of a single
sign on of sorts. The auth stuff still needs some refining I am sure but this whole thing is meant to be my learning
processes after all 🙃

### [Generator](generator)

This is an attempt to help generate the things that are common, things like the base service files, route files, adding
the extra build.gradle dependency and add the appropriate things to the injection classes for things to get wired up
properly. This uses [Clikt](https://ajalt.github.io/clikt/) a kotlin command line tool to capture user input about the
name of the module to create and what shared folder to look at (from WeesnerDevelopment-Shared). This uses templates I
created for the common bits that will be generated.

### [Serial Cabinet](serialCabinet)

This is a service for the app for [Serial Cabinet](https://github.com/adamWeesner/Serial-Cabinet) it is a home inventory
of sorts that is focused on electronics, so things that have serial numbers, model numbers, things like that.

### [Tax Fetcher](taxFetcher)

This is a service to retrieve federal tax information (Social Security, Medicare, Federal Income Tax) for given years.
This kind of data is useful for things like determining how much Federal Income Tax will be taken out of your check,
with a simple formula, for example. There is a front-end I want to build around this. I had one in the past but archived
it as I was not happy with it.[Swagger spec sheet](taxFetcher/swagger.yaml).