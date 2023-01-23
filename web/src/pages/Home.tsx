import { Typography } from "@material-tailwind/react";

export default function Home() {
    return (
        <div className="home">
            <Typography
                as="big"
                variant="h1"
                className="text-5xl mr-4 cursor-pointer py-1.5 font-normal">
                    <b>I am, Because You are.</b>
            </Typography>

            <br />

            <Typography
                as="big"
                variant="h1"
                className="text-3xl mr-4 cursor-pointer py-1.5 font-normal">
                    <span className="bg-purple-600 text-white p-1 rounded">Our app</span> brings an <span className="bg-yellow-600 text-white p-1 rounded">unique set</span> of features that combines <span className="underline underline-offset-4 decoration-purple-600">social media</span>, <span className="underline underline-offset-4 decoration-purple-600">video editing platform</span>, and a <span className="underline underline-offset-4 decoration-purple-600">daily habit tracker</span>.
            </Typography>

            <br />

            <button className="mt-5 mr-2 font-bold text-white bg-black border-2 border-black py-1 px-2 focus:outline-none hover:bg-transparent hover:text-black rounded text-lg">
                <a href="/">Development in Progress.</a>
            </button>

            <button className="mt-5 font-bold text-black bg-transparent border-2 border-black py-1 px-2 focus:outline-none hover:bg-black hover:text-white rounded text-lg">
                <a href="/tech">Our Integration with DALL-E 2.</a>
            </button>
            
            <table className="mt-5"><tr>

                <td valign="middle"><img width="500" alt="Screenshot-3" src="https://user-images.githubusercontent.com/35755386/213597146-7b062310-df93-488d-9dc6-e84036f26eb4.png" /></td>

                <td valign="middle"><img width="500" alt="Screenshot-4" src="https://user-images.githubusercontent.com/35755386/213597161-cd6da778-b4bc-4d40-ba5e-7a4c33982afd.png" /></td>

                <td valign="middle"><img width="500" alt="Screenshot-5" src="https://user-images.githubusercontent.com/35755386/213597165-01129c78-c6d7-4886-b257-91c5bb1b5162.png" /></td>

                <td valign="middle"><img width="500" alt="Screenshot-7" src="https://user-images.githubusercontent.com/35755386/213597182-8f16f327-549c-4aa2-a604-25315db092de.png" /></td>

            </tr></table>
        </div>
    );
}