# Local build dependencies

These jars are **not** included in the repository because FNAFMW is not
distributed through a public Maven and its assets are not ours to redistribute.

Put the following files into this folder before running `gradle build`:

- `fnafmw-alpha-1.0.0-snapshot-1.jar`
- `refraction-neoforge-1.21.1-1.3.0-mw1.jar`
- `vixis-0.7.0.jar`
- `geckolib-neoforge-1.21.1-4.9.2.jar`
- `veil-neoforge-1.21.1-4.5.2.jar`

The Refraction and Vixis jars are bundled inside the FNAFMW jar under
`META-INF/jarjar/`. Extract them if your build needs the classes directly.

The prebuilt addon jar in `dist/` does not need this folder.
