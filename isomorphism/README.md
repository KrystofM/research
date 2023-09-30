# IsomorphismProject

*The contents of this repo have been redacted to guard academic integrity of students pursuing the project in later years. For further information about this project, find my 
contant information on my personal website krystofmitka.com  

## Authors
- Hessel Stokman
- Mikuláš Vanoušek
- Kryštof Mitka
- Jonne Poles

## Running the code
1. Clone this repository (or download the zip file)
2. In `isomorphismproject` create a directory called `input`, or if you wish to call it something else, then change the `folder` parameter in the main function of `main.py`.
   Don't forget to put a / after the folder name.
3. Place your `.gr` and `.grl` files into the directory you are testing
4. Run the script:
   ```bash 
   python main.py
   ```
5. If the filename has the `.gr` extension, automorphisms will be counted
6. If the filename has the `.grl` extension, and contains `GI`, but not `Aut`, only isomorphisms will be identified
6. If the filename has the `.grl` extension, and contains `Aut` and `GI`, both isomorphisms and automorphisms will be identified
7. If the filename has the `.grl` extension, and contains `Aut`, but not `GI`, then only automorphisms will be counted for each graph

